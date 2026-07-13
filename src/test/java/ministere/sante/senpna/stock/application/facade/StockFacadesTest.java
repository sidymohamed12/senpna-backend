package ministere.sante.senpna.stock.application.facade;

import ministere.sante.senpna.stock.domain.command.LotCommands.AlertePeremptionQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.BloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.CreerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.DebloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.GetLotQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.ListLotsQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.domain.command.LotCommands.ModifierPrixLotCommand;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.GetMouvementQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.ListMouvementsQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;
import ministere.sante.senpna.stock.domain.command.StockCommands.DefinirSeuilAlerteCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.EntreeStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.GetStockQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.LibererReservationCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReservationFefoResult;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockFefoCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.SortieStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;
import ministere.sante.senpna.stock.domain.port.in.lot.BloquerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.CreerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.DebloquerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.GetLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.ListLotsUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.ListerAlertesPeremptionUseCase;
import ministere.sante.senpna.stock.domain.port.in.lot.ModifierPrixLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.mouvement.EntreeStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.mouvement.GetMouvementUseCase;
import ministere.sante.senpna.stock.domain.port.in.mouvement.ListMouvementsUseCase;
import ministere.sante.senpna.stock.domain.port.in.mouvement.SortirStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.DefinirSeuilAlerteUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.GetStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.LibererReservationUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ListStocksUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ListerAlertesRuptureUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ReserverStockFefoUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ReserverStockUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Façades du module stock — délégation aux use cases")
class StockFacadesTest {

        @Nested
        @DisplayName("LotFacade")
        class LotFacadeTests {

                @Mock
                CreerLotUseCase creerLotUseCase;
                @Mock
                BloquerLotUseCase bloquerLotUseCase;
                @Mock
                DebloquerLotUseCase debloquerLotUseCase;
                @Mock
                GetLotUseCase getLotUseCase;
                @Mock
                ListLotsUseCase listLotsUseCase;
                @Mock
                ListerAlertesPeremptionUseCase listerAlertesPeremptionUseCase;
                @Mock
                ModifierPrixLotUseCase modifierPrixLotUseCase;

                LotFacade sut;

                @BeforeEach
                void setUp() {
                        sut = new LotFacade(creerLotUseCase, bloquerLotUseCase, debloquerLotUseCase, getLotUseCase,
                                        listLotsUseCase, listerAlertesPeremptionUseCase, modifierPrixLotUseCase);
                }

                @Test
                @DisplayName("chaque méthode délègue au use case correspondant")
                void chaqueMethodeDelegue() {
                        UUID id = UUID.randomUUID();
                        LotDetail detail = new LotDetail(id, "L1", null, null, null, null, null, null, "ACTIF", false,
                                        0, null,
                                        null);
                        LotPage page = new LotPage(List.of(), 0, 20, 0, 0);

                        when(creerLotUseCase.creer(any())).thenReturn(detail);
                        when(bloquerLotUseCase.bloquer(any())).thenReturn(detail);
                        when(debloquerLotUseCase.debloquer(any())).thenReturn(detail);
                        when(getLotUseCase.obtenir(any())).thenReturn(detail);
                        when(listLotsUseCase.lister(any())).thenReturn(page);
                        when(listerAlertesPeremptionUseCase.lister(any())).thenReturn(page);
                        when(modifierPrixLotUseCase.modifierPrix(any())).thenReturn(detail);

                        assertThat(sut.creerLot(new CreerLotCommand("L1", id, id, null, null, null, null)))
                                        .isSameAs(detail);
                        assertThat(sut.bloquerLot(new BloquerLotCommand(id))).isSameAs(detail);
                        assertThat(sut.debloquerLot(new DebloquerLotCommand(id))).isSameAs(detail);
                        assertThat(sut.obtenirLot(new GetLotQuery(id))).isSameAs(detail);
                        assertThat(sut.listerLots(new ListLotsQuery(null, null, null, null, null, 0, 20, null, null)))
                                        .isSameAs(page);
                        assertThat(sut.listerAlertesPeremption(new AlertePeremptionQuery(30, null, null, 0, 20)))
                                        .isSameAs(page);
                        assertThat(sut.modifierPrixLot(new ModifierPrixLotCommand(id, null, null))).isSameAs(detail);
                }
        }

        @Nested
        @DisplayName("StockFacade")
        class StockFacadeTests {

                @Mock
                EntreeStockUseCase entreeStockUseCase;
                @Mock
                SortirStockUseCase sortirStockUseCase;
                @Mock
                ReserverStockUseCase reserverStockUseCase;
                @Mock
                ReserverStockFefoUseCase reserverStockFefoUseCase;
                @Mock
                LibererReservationUseCase libererReservationUseCase;
                @Mock
                GetStockUseCase getStockUseCase;
                @Mock
                ListStocksUseCase listStocksUseCase;
                @Mock
                ListerAlertesRuptureUseCase listerAlertesRuptureUseCase;
                @Mock
                DefinirSeuilAlerteUseCase definirSeuilAlerteUseCase;

                StockFacade sut;

                @BeforeEach
                void setUp() {
                        sut = new StockFacade(entreeStockUseCase, sortirStockUseCase, reserverStockUseCase,
                                        reserverStockFefoUseCase, libererReservationUseCase, getStockUseCase,
                                        listStocksUseCase,
                                        listerAlertesRuptureUseCase, definirSeuilAlerteUseCase);
                }

                @Test
                @DisplayName("chaque méthode délègue au use case correspondant")
                void chaqueMethodeDelegue() {
                        UUID id = UUID.randomUUID();
                        StockDetail detail = new StockDetail(id, null, null, null, null, null, null, null, null, false,
                                        false,
                                        null, null);
                        StockPage page = new StockPage(List.of(), 0, 20, 0, 0);
                        ReservationFefoResult fefoResult = new ReservationFefoResult(id, id, null, null, List.of());

                        when(entreeStockUseCase.entrer(any())).thenReturn(detail);
                        when(sortirStockUseCase.sortir(any())).thenReturn(detail);
                        when(reserverStockUseCase.reserver(any())).thenReturn(detail);
                        when(reserverStockFefoUseCase.reserver(any())).thenReturn(fefoResult);
                        when(libererReservationUseCase.liberer(any())).thenReturn(detail);
                        when(getStockUseCase.obtenir(any())).thenReturn(detail);
                        when(listStocksUseCase.lister(any())).thenReturn(page);
                        when(listerAlertesRuptureUseCase.lister(any())).thenReturn(page);
                        when(definirSeuilAlerteUseCase.definir(any())).thenReturn(detail);

                        assertThat(sut.entrerStock(
                                        new EntreeStockCommand(id, id, null, "ACHAT", null, null, null, null)))
                                        .isSameAs(detail);
                        assertThat(sut.sortirStock(
                                        new SortieStockCommand(id, id, null, "PERTE", false, null, null, null, null,
                                                        null)))
                                        .isSameAs(detail);
                        assertThat(sut.reserverStock(new ReserverStockCommand(id, id, null, null))).isSameAs(detail);
                        assertThat(sut.reserverStockFefo(new ReserverStockFefoCommand(id, id, null, null)))
                                        .isSameAs(fefoResult);
                        assertThat(sut.libererReservation(new LibererReservationCommand(id, id, null, null, null)))
                                        .isSameAs(detail);
                        assertThat(sut.obtenirStock(new GetStockQuery(id))).isSameAs(detail);
                        assertThat(sut.listerStocks(
                                        new ListStocksQuery(null, null, null, null, null, 0, 20, null, null)))
                                        .isSameAs(page);
                        assertThat(sut.listerAlertesRupture(
                                        new ListStocksQuery(null, null, null, null, null, 0, 20, null,
                                                        null)))
                                        .isSameAs(page);
                        assertThat(sut.definirSeuilAlerte(new DefinirSeuilAlerteCommand(id, null))).isSameAs(detail);
                }
        }

        @Nested
        @DisplayName("MouvementStockFacade")
        class MouvementStockFacadeTests {

                @Mock
                GetMouvementUseCase getMouvementUseCase;
                @Mock
                ListMouvementsUseCase listMouvementsUseCase;

                MouvementStockFacade sut;

                @BeforeEach
                void setUp() {
                        sut = new MouvementStockFacade(getMouvementUseCase, listMouvementsUseCase);
                }

                @Test
                @DisplayName("chaque méthode délègue au use case correspondant")
                void chaqueMethodeDelegue() {
                        UUID id = UUID.randomUUID();
                        MouvementDetail detail = new MouvementDetail(id, null, null, null, null, null, id, id, null,
                                        null,
                                        null, null, null, null);
                        MouvementPage page = new MouvementPage(List.of(), 0, 20, 0, 0);

                        when(getMouvementUseCase.obtenir(any())).thenReturn(detail);
                        when(listMouvementsUseCase.lister(any())).thenReturn(page);

                        assertThat(sut.obtenirMouvement(new GetMouvementQuery(id))).isSameAs(detail);
                        assertThat(sut.listerMouvements(
                                        new ListMouvementsQuery(null, null, null, null, null, null, null, null,
                                                        0, 20, null, null)))
                                        .isSameAs(page);
                }
        }
}
