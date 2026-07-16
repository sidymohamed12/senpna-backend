package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.CatalogueAggregatRow;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.CatalogueStockJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockAgregeQueryAdapter — unitaire")
class StockAgregeQueryAdapterTest {

    @Mock
    CatalogueStockJpaRepository catalogueStockJpaRepository;

    @Mock
    CatalogueAggregatRow row;

    StockAgregeQueryAdapter adapter;

    UUID entrepotId = UUID.randomUUID();
    UUID medicamentId = UUID.randomUUID();
    UUID fournisseurId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new StockAgregeQueryAdapter(catalogueStockJpaRepository);
    }

    private void stubRow() {
        when(row.getEntrepotId()).thenReturn(entrepotId);
        when(row.getMedicamentId()).thenReturn(medicamentId);
        when(row.getQuantiteDisponible()).thenReturn(new BigDecimal("100.0000"));
        when(row.getQuantiteReservee()).thenReturn(new BigDecimal("10.0000"));
        when(row.getNombreLotsActifs()).thenReturn(3L);
        when(row.getProchaineDateExpiration()).thenReturn(LocalDate.now().plusMonths(6));
        when(row.getPrixVenteMoyen()).thenReturn(new BigDecimal("15.50"));
        when(row.getFournisseurId()).thenReturn(fournisseurId);
    }

    @Test
    @DisplayName("rechercherParEntrepot() délègue à rechercherParEntrepots() avec un ensemble à un élément")
    void rechercherParEntrepot() {
        stubRow();
        when(catalogueStockJpaRepository.agregerParEntrepots(Set.of(entrepotId))).thenReturn(List.of(row));

        List<StockAgregeProjection> result = adapter.rechercherParEntrepot(entrepotId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).entrepotId()).isEqualTo(entrepotId);
        assertThat(result.get(0).medicamentId()).isEqualTo(medicamentId);
        assertThat(result.get(0).nombreLotsActifs()).isEqualTo(3);
        assertThat(result.get(0).fournisseurId()).isEqualTo(fournisseurId);
    }

    @Test
    @DisplayName("rechercherParEntrepots() mappe chaque ligne agrégée, nombreLotsActifs null → 0")
    void rechercherParEntrepots_nombreLotsActifsNul() {
        when(row.getEntrepotId()).thenReturn(entrepotId);
        when(row.getMedicamentId()).thenReturn(medicamentId);
        when(row.getQuantiteDisponible()).thenReturn(BigDecimal.ZERO);
        when(row.getQuantiteReservee()).thenReturn(BigDecimal.ZERO);
        when(row.getNombreLotsActifs()).thenReturn(null);
        when(row.getProchaineDateExpiration()).thenReturn(null);
        when(row.getPrixVenteMoyen()).thenReturn(null);
        when(row.getFournisseurId()).thenReturn(null);
        when(catalogueStockJpaRepository.agregerParEntrepots(Set.of(entrepotId))).thenReturn(List.of(row));

        List<StockAgregeProjection> result = adapter.rechercherParEntrepots(Set.of(entrepotId));

        assertThat(result.get(0).nombreLotsActifs()).isZero();
    }

    @Test
    @DisplayName("rechercherParEntrepots() avec ensemble null → liste vide, sans appel repository")
    void rechercherParEntrepots_ensembleNul() {
        assertThat(adapter.rechercherParEntrepots(null)).isEmpty();
        verifyNoInteractions(catalogueStockJpaRepository);
    }

    @Test
    @DisplayName("rechercherParEntrepots() avec ensemble vide → liste vide, sans appel repository")
    void rechercherParEntrepots_ensembleVide() {
        assertThat(adapter.rechercherParEntrepots(Set.of())).isEmpty();
        verifyNoInteractions(catalogueStockJpaRepository);
    }
}
