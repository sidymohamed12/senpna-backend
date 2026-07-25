package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueInterPraQuery;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsulterCatalogueInterPraUseCaseImpl — consultation du catalogue inter-PRA")
class ConsulterCatalogueInterPraUseCaseImplTest {

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

    ConsulterCatalogueInterPraUseCaseImpl sut;

    UUID entrepot1;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(null, null, null, null,
                new CacheProperties(null, null, null, null, null, Duration.ofMinutes(1), null, null));

        sut = new ConsulterCatalogueInterPraUseCaseImpl(catalogueAccessGuard, entrepotQueryPort,
                stockAgregeQueryPort, medicamentQueryPort, catalogueEntryAssembler, cache, appProperties);

        entrepot1 = UUID.randomUUID();
    }

    private ConsulterCatalogueInterPraQuery query() {
        return new ConsulterCatalogueInterPraQuery(null, null, null, null, null);
    }

    @Nested
    @DisplayName("contrôle d'accès")
    class ControleAcces {

        @Test
        @DisplayName("vérifie systématiquement que l'acteur est PNA ou PRA avant toute lecture")
        void verifieActeurPnaOuPra() {
            when(cache.get(anyString(), eq(CatalogueInterPraPage.class))).thenReturn(Optional.of(
                    new CatalogueInterPraPage(List.of(), 0, 20, 0, 0)));

            sut.consulter(query());

            verify(catalogueAccessGuard).verifierActeurPnaOuPra();
        }
    }

    @Nested
    @DisplayName("cache")
    class Cache {

        @Test
        @DisplayName("entrée en cache présente → renvoyée directement, aucun assemblage")
        void cacheHit_pasAssemblage() {
            CatalogueInterPraPage pageEnCache = new CatalogueInterPraPage(List.of(), 0, 20, 0, 0);
            when(cache.get(anyString(), eq(CatalogueInterPraPage.class))).thenReturn(Optional.of(pageEnCache));

            CatalogueInterPraPage resultat = sut.consulter(query());

            assertThat(resultat).isSameAs(pageEnCache);
            verify(entrepotQueryPort, never()).findPrasActives();
            verify(cache, never()).put(anyString(), any(), any());
        }

        @Test
        @DisplayName("cache miss → assemble puis écrit le résultat en cache")
        void cacheMiss_assembleEtEcrit() {
            when(cache.get(anyString(), eq(CatalogueInterPraPage.class))).thenReturn(Optional.empty());
            when(entrepotQueryPort.findPrasActives()).thenReturn(List.of(
                    new EntrepotProjection(entrepot1, "PRA-DK", "PRA Dakar", "PRA", UUID.randomUUID(), true)));
            when(stockAgregeQueryPort.rechercherParEntrepots(anySet())).thenReturn(List.of());
            CatalogueInterPraPage pageAssemblee = new CatalogueInterPraPage(List.of(), 0, 20, 0, 0);
            when(catalogueEntryAssembler.assemblerInterPra(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(pageAssemblee);

            CatalogueInterPraPage resultat = sut.consulter(query());

            assertThat(resultat).isSameAs(pageAssemblee);
            verify(cache).put(anyString(), eq(pageAssemblee), eq(Duration.ofMinutes(1)));
        }
    }

    @Nested
    @DisplayName("aucune PRA active")
    class AucunePraActive {

        @Test
        @DisplayName("aucune PRA active → page vide, ni stock ni assemblage sollicités")
        void aucunePraActive_pageVide() {
            when(cache.get(anyString(), eq(CatalogueInterPraPage.class))).thenReturn(Optional.empty());
            when(entrepotQueryPort.findPrasActives()).thenReturn(List.of());

            CatalogueInterPraPage resultat = sut.consulter(query());

            assertThat(resultat.content()).isEmpty();
            assertThat(resultat.totalElements()).isZero();
            verify(stockAgregeQueryPort, never()).rechercherParEntrepots(anySet());
            verify(catalogueEntryAssembler, never())
                    .assemblerInterPra(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("aucune PRA active avec une taille de page demandée → la taille demandée est respectée")
        void aucunePraActive_tailleDemandeeRespectee() {
            when(cache.get(anyString(), eq(CatalogueInterPraPage.class))).thenReturn(Optional.empty());
            when(entrepotQueryPort.findPrasActives()).thenReturn(List.of());

            ConsulterCatalogueInterPraQuery query = new ConsulterCatalogueInterPraQuery(null, null, null, null, 50);

            CatalogueInterPraPage resultat = sut.consulter(query);

            assertThat(resultat.size()).isEqualTo(50);
        }
    }

}
