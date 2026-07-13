package ministere.sante.senpna.stock.application.usecase.mouvement;

import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.SortieStockCommand;
import ministere.sante.senpna.stock.domain.exception.lot.LotNonDisponibleException;
import ministere.sante.senpna.stock.domain.exception.stock.StockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SortirStockUseCaseImpl — sortie de stock")
class SortirStockUseCaseImplTest {

    @Mock
    StockRepositoryPort stockRepositoryPort;
    @Mock
    LotRepositoryPort lotRepositoryPort;
    @Mock
    EntrepotRepositoryPort entrepotRepositoryPort;
    @Mock
    MouvementStockRepositoryPort mouvementStockRepositoryPort;
    @Mock
    StockDetailAssembler stockDetailAssembler;
    @Mock
    EntrepotScopeGuard entrepotScopeGuard;

    SortirStockUseCaseImpl sut;

    Entrepot entrepot;
    Lot lot;

    @BeforeEach
    void setUp() {
        sut = new SortirStockUseCaseImpl(stockRepositoryPort, lotRepositoryPort, entrepotRepositoryPort,
                mouvementStockRepositoryPort, stockDetailAssembler, entrepotScopeGuard);
        entrepot = Entrepot.creerPra("PRA-1", "PRA 1", RegionId.generate(), "Adresse", "771111111");
        lot = Lot.creer("LOT-1", MedicamentId.generate(), FournisseurId.generate(), LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN);
    }

    private SortieStockCommand commande(String type, boolean depuisReservation) {
        UUID entrepotDestinationId = UUID.randomUUID(); // requis pour SORTIE_TRANSFERT
        return new SortieStockCommand(entrepot.getId().getValue(), lot.getId().getValue(), new BigDecimal("10"),
                type, depuisReservation, entrepotDestinationId, null, "REF", "Motif", UUID.randomUUID());
    }

    @Test
    @DisplayName("sortie d'expédition (SORTIE_TRANSFERT) avec un lot bloqué → LotNonDisponibleException")
    void sortieExpeditionLotBloque_leveException() {
        lot.bloquer();
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepot));

        assertThatThrownBy(() -> sut.sortir(commande("SORTIE_TRANSFERT", false)))
                .isInstanceOf(LotNonDisponibleException.class);
    }

    @Test
    @DisplayName("sortie corrective (PERTE) avec un lot bloqué → autorisée quand même")
    void sortieCorrectiveLotBloque_autorisee() {
        lot.bloquer();
        Stock stock = Stock.ouvrir(entrepot.getId(), lot.getId(), lot.getMedicamentId(), null);
        stock.entrer(new BigDecimal("50"));
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepot));
        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any())).thenReturn(Optional.of(stock));
        when(stockRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.sortir(commande("PERTE", false));

        assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo("40");
    }

    @Test
    @DisplayName("aucune ligne de stock trouvée → StockIntrouvableException")
    void aucuneLigneStock_leveException() {
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepot));
        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.sortir(commande("PERTE", false)))
                .isInstanceOf(StockIntrouvableException.class);
    }

    @Test
    @DisplayName("depuisReservation=true → utilise sortirDepuisReservation() au lieu de sortir()")
    void depuisReservation_utiliseMethodeAdequate() {
        Stock stock = Stock.ouvrir(entrepot.getId(), lot.getId(), lot.getMedicamentId(), null);
        stock.entrer(new BigDecimal("50"));
        stock.reserver(new BigDecimal("20"));
        when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
        when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepot));
        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any())).thenReturn(Optional.of(stock));
        when(stockRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.sortir(commande("SORTIE_TRANSFERT", true));

        // sortirDepuisReservation() diminue quantiteDisponible ET quantiteReservee
        assertThat(stock.getQuantiteReservee()).isEqualByComparingTo("10");
        assertThat(stock.getQuantiteDisponible()).isEqualByComparingTo("40");
    }
}
