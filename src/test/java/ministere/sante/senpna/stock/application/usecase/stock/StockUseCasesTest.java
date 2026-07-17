package ministere.sante.senpna.stock.application.usecase.stock;

import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.DefinirSeuilAlerteCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.GetStockQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.LibererReservationCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockCommand;
import ministere.sante.senpna.stock.domain.exception.lot.LotNonDisponibleException;
import ministere.sante.senpna.stock.domain.exception.stock.StockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Use cases unitaires du module stock")
class StockUseCasesTest {

    @Mock
    StockRepositoryPort stockRepositoryPort;
    @Mock
    LotRepositoryPort lotRepositoryPort;
    @Mock
    StockDetailAssembler stockDetailAssembler;
    @Mock
    EntrepotScopeGuard entrepotScopeGuard;

    EntrepotId entrepotId;
    LotId lotId;
    Stock stock;

    @BeforeEach
    void setUp() {
        entrepotId = EntrepotId.generate();
        lotId = LotId.generate();
        stock = Stock.ouvrir(new Stock.OuvertureCommand(entrepotId, lotId, MedicamentId.generate(), null));
        stock.entrer(new BigDecimal("100"));
    }

    @Nested
    @DisplayName("GetStockUseCaseImpl")
    class Get {

        GetStockUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetStockUseCaseImpl(stockRepositoryPort, stockDetailAssembler, entrepotScopeGuard);
        }

        @Test
        @DisplayName("vérifie la lecture autorisée après avoir trouvé le stock")
        void verifieLectureAutorisee() {
            when(stockRepositoryPort.findById(StockId.of(stock.getId().getValue()))).thenReturn(Optional.of(stock));

            sut.obtenir(new GetStockQuery(stock.getId().getValue()));

            verify(entrepotScopeGuard).verifierLectureAutorisee(entrepotId.getValue());
        }

        @Test
        @DisplayName("introuvable → StockIntrouvableException")
        void introuvable_leveException() {
            when(stockRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var getStockQuery = new GetStockQuery(UUID.randomUUID());
            assertThatThrownBy(() -> sut.obtenir(getStockQuery))
                    .isInstanceOf(StockIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("DefinirSeuilAlerteUseCaseImpl")
    class DefinirSeuilAlerte {

        @Test
        @DisplayName("définit le nouveau seuil d'alerte")
        void definitNouveauSeuil() {
            when(stockRepositoryPort.findById(StockId.of(stock.getId().getValue()))).thenReturn(Optional.of(stock));
            when(stockRepositoryPort.save(stock)).thenReturn(stock);

            new DefinirSeuilAlerteUseCaseImpl(stockRepositoryPort, stockDetailAssembler, entrepotScopeGuard)
                    .definir(new DefinirSeuilAlerteCommand(stock.getId().getValue(), new BigDecimal("25")));

            assertThat(stock.getSeuilAlerte()).isEqualByComparingTo("25");
        }
    }

    @Nested
    @DisplayName("ReserverStockUseCaseImpl")
    class Reserver {

        ReserverStockUseCaseImpl sut;
        Lot lot;

        @BeforeEach
        void setUp() {
            sut = new ReserverStockUseCaseImpl(stockRepositoryPort, lotRepositoryPort, stockDetailAssembler,
                    entrepotScopeGuard);
            lot = Lot.creer(new Lot.CreationCommand("LOT-1", MedicamentId.generate(), FournisseurId.generate(),
                    LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), BigDecimal.TEN, BigDecimal.TEN));
        }

        @Test
        @DisplayName("lot bloqué → LotNonDisponibleException")
        void lotBloque_leveException() {
            lot.bloquer();
            when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));

            var reserverStockCommand = new ReserverStockCommand(entrepotId.getValue(), lot.getId().getValue(),
                    BigDecimal.TEN, null);
            assertThatThrownBy(() -> sut.reserver(reserverStockCommand))
                    .isInstanceOf(LotNonDisponibleException.class);
        }

        @Test
        @DisplayName("réservation valide → augmente la quantité réservée")
        void reservationValide_augmenteQuantiteReservee() {
            when(lotRepositoryPort.findById(any())).thenReturn(Optional.of(lot));
            when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any())).thenReturn(Optional.of(stock));
            when(stockRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            sut.reserver(new ReserverStockCommand(entrepotId.getValue(), lot.getId().getValue(),
                    new BigDecimal("30"), null));

            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo("30");
        }
    }

    @Nested
    @DisplayName("LibererReservationUseCaseImpl")
    class LibererReservation {

        @Test
        @DisplayName("libère la quantité réservée précédemment")
        void libereQuantiteReservee() {
            stock.reserver(new BigDecimal("40"));
            when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any())).thenReturn(Optional.of(stock));
            when(stockRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            new LibererReservationUseCaseImpl(stockRepositoryPort, stockDetailAssembler, entrepotScopeGuard)
                    .liberer(new LibererReservationCommand(entrepotId.getValue(), lotId.getValue(),
                            new BigDecimal("15"), null, "Commande annulée"));

            assertThat(stock.getQuantiteReservee()).isEqualByComparingTo("25");
        }

        @Test
        @DisplayName("aucune ligne de stock trouvée → StockIntrouvableException")
        void aucuneLigne_leveException() {
            when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(any(), any())).thenReturn(Optional.empty());

            var sut = new LibererReservationUseCaseImpl(stockRepositoryPort, stockDetailAssembler, entrepotScopeGuard);
            var libererReservationCommand = new LibererReservationCommand(entrepotId.getValue(), lotId.getValue(),
                    BigDecimal.TEN, null, "Motif");
            assertThatThrownBy(() -> sut.liberer(libererReservationCommand))
                    .isInstanceOf(StockIntrouvableException.class);
        }
    }
}
