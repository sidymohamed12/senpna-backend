package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueRegionalQuery;
import ministere.sante.senpna.catalogue.domain.exception.AucunePraPourRegionException;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.config.AppProperties.CacheProperties;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.port.out.StockAgregeQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsulterCatalogueRegionalUseCaseImpl — consultation du catalogue régional")
class ConsulterCatalogueRegionalUseCaseImplTest {

    @Mock
    CatalogueAccessGuard catalogueAccessGuard;
    @Mock
    EntrepotQueryPort entrepotQueryPort;
    @Mock
    StockAgregeQueryPort stockAgregeQueryPort;
    @Mock
    MedicamentQueryPort medicamentQueryPort;
    @Mock
    CatalogueEntryAssembler catalogueEntryAssembler;
    @Mock
    JsonCacheSupport cache;

    ConsulterCatalogueRegionalUseCaseImpl sut;

    UUID regionThies;
    UUID pnaId;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(null, null, null, null,
                new CacheProperties(null, null, null, null, null, Duration.ofMinutes(1), null, null));

        sut = new ConsulterCatalogueRegionalUseCaseImpl(catalogueAccessGuard, entrepotQueryPort,
                stockAgregeQueryPort, medicamentQueryPort, catalogueEntryAssembler, cache, appProperties);

        regionThies = UUID.randomUUID();
        pnaId = UUID.randomUUID();
    }

    private ConsulterCatalogueRegionalQuery query(UUID regionId) {
        return new ConsulterCatalogueRegionalQuery(regionId, null, null, null, null);
    }

    @Nested
    @DisplayName("résolution de région")
    class ResolutionRegion {

        @Test
        @DisplayName("résout la région AVANT toute lecture de cache, en délégant au guard")
        void resoutRegionAvantCache() {
            when(catalogueAccessGuard.resoudreRegionPourCatalogueRegional(regionThies)).thenReturn(regionThies);
            when(cache.get(anyString(), eq(CataloguePage.class))).thenReturn(Optional.of(
                    new CataloguePage(List.of(), 0, 20, 0, 0)));

            sut.consulter(query(regionThies));

            verify(catalogueAccessGuard).resoudreRegionPourCatalogueRegional(regionThies);
        }

        @Test
        @DisplayName("la clé de cache est segmentée par la région EFFECTIVE, pas la région demandée")
        void cleCacheSegmenteeParRegionEffective() {
            UUID regionEffective = UUID.randomUUID();
            when(catalogueAccessGuard.resoudreRegionPourCatalogueRegional(regionThies)).thenReturn(regionEffective);
            when(cache.get(anyString(), eq(CataloguePage.class))).thenReturn(Optional.of(
                    new CataloguePage(List.of(), 0, 20, 0, 0)));

            sut.consulter(query(regionThies));

            verify(cache).get(org.mockito.ArgumentMatchers.contains(regionEffective.toString()),
                    eq(CataloguePage.class));
        }
    }

    @Nested
    @DisplayName("cache")
    class Cache {

        @Test
        @DisplayName("cache hit → renvoyé directement, aucun accès à l'entrepôt")
        void cacheHit_pasAccesEntrepot() {
            when(catalogueAccessGuard.resoudreRegionPourCatalogueRegional(regionThies)).thenReturn(regionThies);
            CataloguePage pageEnCache = new CataloguePage(List.of(), 0, 20, 0, 0);
            when(cache.get(anyString(), eq(CataloguePage.class))).thenReturn(Optional.of(pageEnCache));

            CataloguePage resultat = sut.consulter(query(regionThies));

            assertThat(resultat).isSameAs(pageEnCache);
            verify(entrepotQueryPort, never()).findPrasActivesParRegion(any());
        }

        @Test
        @DisplayName("cache miss → assemble puis écrit en cache avec le TTL configuré")
        void cacheMiss_assembleEtEcrit() {
            UUID praId = UUID.randomUUID();
            when(catalogueAccessGuard.resoudreRegionPourCatalogueRegional(regionThies)).thenReturn(regionThies);
            when(cache.get(anyString(), eq(CataloguePage.class))).thenReturn(Optional.empty());
            when(entrepotQueryPort.findPrasActivesParRegion(regionThies)).thenReturn(List.of(
                    new EntrepotProjection(praId, "PRA-THIES", "PRA Thiès", "PRA", regionThies, true)));
            when(stockAgregeQueryPort.rechercherParEntrepot(praId)).thenReturn(List.of());
            CataloguePage pageAssemblee = new CataloguePage(List.of(), 0, 20, 0, 0);
            when(catalogueEntryAssembler.assembler(any(), any(), any(), any(), any(), any()))
                    .thenReturn(pageAssemblee);

            CataloguePage resultat = sut.consulter(query(regionThies));

            assertThat(resultat).isSameAs(pageAssemblee);
            verify(cache).put(anyString(), eq(pageAssemblee), eq(Duration.ofMinutes(1)));
        }
    }

    @Nested
    @DisplayName("aucune PRA pour la région")
    class AucunePraPourRegion {

        @Test
        @DisplayName("aucune PRA active dans la région → AucunePraPourRegionException")
        void aucunePraActive_leveException() {
            when(catalogueAccessGuard.resoudreRegionPourCatalogueRegional(regionThies)).thenReturn(regionThies);
            when(cache.get(anyString(), eq(CataloguePage.class))).thenReturn(Optional.empty());
            when(entrepotQueryPort.findPrasActivesParRegion(regionThies)).thenReturn(List.of());

            var query = query(regionThies);
            assertThatThrownBy(() -> sut.consulter(query))
                    .isInstanceOf(AucunePraPourRegionException.class);
        }
    }
}
