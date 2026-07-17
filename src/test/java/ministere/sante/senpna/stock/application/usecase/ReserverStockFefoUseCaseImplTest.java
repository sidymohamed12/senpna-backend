package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReservationFefoResult;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockFefoCommand;
import ministere.sante.senpna.stock.domain.exception.stock.StockInsuffisantException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.usecase.stock.ReserverStockFefoUseCaseImpl;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReserverStockFefoUseCase — réservation automatique FEFO")
class ReserverStockFefoUseCaseImplTest {

        private static final EntrepotId ENTREPOT_ID = EntrepotId.generate();
        private static final MedicamentId MEDICAMENT_ID = MedicamentId.generate();
        private static final FournisseurId FOURNISSEUR_ID = FournisseurId.generate();

        @Mock
        private LotRepositoryPort lotRepositoryPort;

        @Mock
        private StockRepositoryPort stockRepositoryPort;

        @Mock
        private EntrepotRepositoryPort entrepotRepositoryPort;

        @Mock
        private EntrepotScopeGuard entrepotScopeGuard;

        private ReserverStockFefoUseCaseImpl useCase;

        private static Lot lot(String numero, LocalDate expiration) {
                return Lot.creer(new Lot.CreationCommand(numero, MEDICAMENT_ID, FOURNISSEUR_ID, null, expiration, null, null));
        }

        private static Stock stockAvecQuantite(LotId lotId, BigDecimal quantite) {
                Stock stock = Stock.ouvrir(new Stock.OuvertureCommand(ENTREPOT_ID, lotId, MEDICAMENT_ID, null));
                stock.entrer(quantite);
                return stock;
        }

        private static Entrepot entrepotPra() {
                return Entrepot.builder()
                    .id(ENTREPOT_ID)
                    .code("PRA-THIES")
                    .nom("PRA Thiès")
                    .type(TypeEntrepot.PRA)
                    .regionId(RegionId.generate())
                    .adresse(null)
                    .telephone(null)
                    .responsableUserId(null)
                    .actif(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }

        @Nested
        @DisplayName("reserver()")
        class Reserver {

                @Test
                @DisplayName("consomme le lot expirant le plus tôt en premier (FEFO)")
                void reserver_consommeLePlusAncienDabord() {
                        useCase = new ReserverStockFefoUseCaseImpl(lotRepositoryPort, stockRepositoryPort,
                                        entrepotRepositoryPort, entrepotScopeGuard);

                        Lot lotProche = lot("LOT-PROCHE", LocalDate.now().plusDays(30));
                        Lot lotLointain = lot("LOT-LOINTAIN", LocalDate.now().plusDays(365));

                        Stock stockProche = stockAvecQuantite(lotProche.getId(), new BigDecimal("50"));
                        Stock stockLointain = stockAvecQuantite(lotLointain.getId(), new BigDecimal("100"));

                        when(entrepotRepositoryPort.findById(ENTREPOT_ID)).thenReturn(Optional.of(entrepotPra()));
                        when(lotRepositoryPort.findActifsNonExpiresParMedicamentTriesFefo(MEDICAMENT_ID))
                                        .thenReturn(List.of(lotProche, lotLointain));
                        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(ENTREPOT_ID, lotProche.getId()))
                                        .thenReturn(Optional.of(stockProche));
                        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(ENTREPOT_ID, lotLointain.getId()))
                                        .thenReturn(Optional.of(stockLointain));
                        when(stockRepositoryPort.save(any(Stock.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        ReservationFefoResult result = useCase
                                        .reserver(new ReserverStockFefoCommand(ENTREPOT_ID.getValue(),
                                                        MEDICAMENT_ID.getValue(), new BigDecimal("70"), null));

                        assertThat(result.estEntierementSatisfaite()).isTrue();
                        assertThat(result.allocations()).hasSize(2);
                        // Le lot le plus proche de la péremption est entièrement consommé en premier
                        // (50), le solde (20) provient du lot suivant.
                        assertThat(result.allocations().get(0).lotId()).isEqualTo(lotProche.getId().getValue());
                        assertThat(result.allocations().get(0).quantiteAllouee())
                                        .isEqualByComparingTo(new BigDecimal("50"));
                        assertThat(result.allocations().get(1).lotId()).isEqualTo(lotLointain.getId().getValue());
                        assertThat(result.allocations().get(1).quantiteAllouee())
                                        .isEqualByComparingTo(new BigDecimal("20"));

                        verify(stockRepositoryPort, times(2)).save(any(Stock.class));
                }

                @Test
                @DisplayName("quantité insuffisante sur l'ensemble des lots → StockInsuffisantException")
                void reserver_stockInsuffisant_leveException() {
                        useCase = new ReserverStockFefoUseCaseImpl(lotRepositoryPort, stockRepositoryPort,
                                        entrepotRepositoryPort, entrepotScopeGuard);

                        Lot lotUnique = lot("LOT-UNIQUE", LocalDate.now().plusDays(90));
                        Stock stock = stockAvecQuantite(lotUnique.getId(), new BigDecimal("10"));

                        when(entrepotRepositoryPort.findById(ENTREPOT_ID)).thenReturn(Optional.of(entrepotPra()));
                        when(lotRepositoryPort.findActifsNonExpiresParMedicamentTriesFefo(MEDICAMENT_ID))
                                        .thenReturn(List.of(lotUnique));
                        when(stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(ENTREPOT_ID, lotUnique.getId()))
                                        .thenReturn(Optional.of(stock));
                        when(stockRepositoryPort.save(any(Stock.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        var reserverStockFefoCommand = new ReserverStockFefoCommand(ENTREPOT_ID.getValue(), MEDICAMENT_ID.getValue(), new BigDecimal("25"), null);
                        assertThatThrownBy(() -> useCase.reserver(reserverStockFefoCommand))
                                        .isInstanceOf(StockInsuffisantException.class);
                }

                @Test
                @DisplayName("aucun lot disponible → StockInsuffisantException")
                void reserver_aucunLot_leveException() {
                        useCase = new ReserverStockFefoUseCaseImpl(lotRepositoryPort, stockRepositoryPort,
                                        entrepotRepositoryPort, entrepotScopeGuard);

                        when(entrepotRepositoryPort.findById(ENTREPOT_ID)).thenReturn(Optional.of(entrepotPra()));
                        when(lotRepositoryPort.findActifsNonExpiresParMedicamentTriesFefo(MEDICAMENT_ID))
                                        .thenReturn(List.of());

                        var reserverStockFefoCommand = new ReserverStockFefoCommand(ENTREPOT_ID.getValue(), MEDICAMENT_ID.getValue(), new BigDecimal("5"), null);
                        assertThatThrownBy(() -> useCase.reserver(reserverStockFefoCommand))
                                        .isInstanceOf(StockInsuffisantException.class);
                }
        }
}
