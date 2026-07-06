package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueNationalQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.LigneCatalogue;
import ministere.sante.senpna.catalogue.domain.exception.CatalogueAccesRefuseException;
import ministere.sante.senpna.catalogue.domain.exception.PnaCentraleIntrouvableException;
import ministere.sante.senpna.shared.domain.port.out.ConditionnementQueryPort;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.port.out.StockAgregeQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;
import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.port.out.CachePort;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsulterCatalogueNationalUseCaseImpl")
class ConsulterCatalogueNationalUseCaseImplTest {

    @Mock
    CatalogueAccessGuard catalogueAccessGuard;
    @Mock
    EntrepotQueryPort entrepotQueryPort;
    @Mock
    StockAgregeQueryPort stockAgregeQueryPort;
    @Mock
    MedicamentQueryPort medicamentQueryPort;
    @Mock
    FournisseurCachePort fournisseurCachePort;
    @Mock
    ConditionnementQueryPort conditionnementQueryPort;
    @Mock
    CachePort cachePort;

    CatalogueEntryAssembler catalogueEntryAssembler;

    ConsulterCatalogueNationalUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        catalogueEntryAssembler = new CatalogueEntryAssembler(fournisseurCachePort, conditionnementQueryPort);
        JsonCacheSupport cache = new JsonCacheSupport(cachePort, new com.fasterxml.jackson.databind.ObjectMapper());
        AppProperties appProperties = new AppProperties(null, null, null, null, null,
                new AppProperties.CacheProperties(null, null, null, null, null, java.time.Duration.ofMinutes(1)));
        sut = new ConsulterCatalogueNationalUseCaseImpl(catalogueAccessGuard, entrepotQueryPort,
                stockAgregeQueryPort, medicamentQueryPort, catalogueEntryAssembler, cache, appProperties);
    }

    @Test
    @DisplayName("acteur non autorisé (structure sanitaire) → CatalogueAccesRefuseException, aucune requête stock")
    void acteurNonAutorise_refuse() {
        doThrow(new CatalogueAccesRefuseException()).when(catalogueAccessGuard).verifierActeurPnaOuPra();

        assertThatThrownBy(() -> sut.consulter(new ConsulterCatalogueNationalQuery(null, null, 0, 20)))
                .isInstanceOf(CatalogueAccesRefuseException.class);

        verify(stockAgregeQueryPort, never()).rechercherParEntrepot(any());
    }

    @Test
    @DisplayName("aucun entrepôt PNA centrale actif configuré → PnaCentraleIntrouvableException")
    void aucunePnaCentrale_leveException() {
        when(entrepotQueryPort.findPnaCentraleActive()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.consulter(new ConsulterCatalogueNationalQuery(null, null, 0, 20)))
                .isInstanceOf(PnaCentraleIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal → agrège le stock de la PNA centrale, enrichit médicament/fournisseur/conditionnements")
    void casNominal_retourneCataloguePeuple() {
        UUID entrepotPnaId = UUID.randomUUID();
        EntrepotProjection pnaCentrale = new EntrepotProjection(entrepotPnaId, "PNA-CENTRAL", "PNA Centrale",
                "PNA_CENTRAL", null, true);
        when(entrepotQueryPort.findPnaCentraleActive()).thenReturn(Optional.of(pnaCentrale));

        UUID medicamentId = UUID.randomUUID();
        UUID fournisseurId = UUID.randomUUID();
        StockAgregeProjection ligne = new StockAgregeProjection(entrepotPnaId, medicamentId,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(200), 3, null, BigDecimal.valueOf(150), fournisseurId);
        when(stockAgregeQueryPort.rechercherParEntrepot(entrepotPnaId)).thenReturn(List.of(ligne));

        MedicamentProjection medicament = new MedicamentProjection(medicamentId, "PARA-500", "Doliprane",
                "Paracétamol", "Antalgique", "Sanofi", true);
        when(medicamentQueryPort.findAllById(any())).thenReturn(List.of(medicament));

        when(fournisseurCachePort.findById(fournisseurId))
                .thenReturn(Optional.of(new FournisseurProjection(fournisseurId, "Laboratoire A", true)));
        when(conditionnementQueryPort.findAllVendablesByMedicamentIdIn(any())).thenReturn(List.of());

        CataloguePage page = sut.consulter(new ConsulterCatalogueNationalQuery(null, null, 0, 20));

        assertThat(page.content()).hasSize(1);
        LigneCatalogue ligneCatalogue = page.content().get(0);
        assertThat(ligneCatalogue.medicamentId()).isEqualTo(medicamentId);
        assertThat(ligneCatalogue.nomCommercial()).isEqualTo("Doliprane");
        assertThat(ligneCatalogue.familleNom()).isEqualTo("Antalgique");
        assertThat(ligneCatalogue.fabricant()).isEqualTo("Sanofi");
        assertThat(ligneCatalogue.fournisseurNom()).isEqualTo("Laboratoire A");
        assertThat(ligneCatalogue.quantiteDisponibleALaVente()).isEqualByComparingTo(BigDecimal.valueOf(800));
        assertThat(ligneCatalogue.enRupture()).isFalse();
    }
}
