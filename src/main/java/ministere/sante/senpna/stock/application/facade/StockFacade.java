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
import ministere.sante.senpna.stock.domain.port.in.BloquerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.CreerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.DebloquerLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.DefinirSeuilAlerteUseCase;
import ministere.sante.senpna.stock.domain.port.in.EntreeStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.GetLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.GetMouvementUseCase;
import ministere.sante.senpna.stock.domain.port.in.GetStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.LibererReservationUseCase;
import ministere.sante.senpna.stock.domain.port.in.ListLotsUseCase;
import ministere.sante.senpna.stock.domain.port.in.ListMouvementsUseCase;
import ministere.sante.senpna.stock.domain.port.in.ListStocksUseCase;
import ministere.sante.senpna.stock.domain.port.in.ListerAlertesPeremptionUseCase;
import ministere.sante.senpna.stock.domain.port.in.ListerAlertesRuptureUseCase;
import ministere.sante.senpna.stock.domain.port.in.ModifierPrixLotUseCase;
import ministere.sante.senpna.stock.domain.port.in.ReserverStockFefoUseCase;
import ministere.sante.senpna.stock.domain.port.in.ReserverStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.SortirStockUseCase;

import org.springframework.stereotype.Component;

@Component
public class StockFacade {

    // ── Lot ──────────────────────────────────────────────────────────────
    private final CreerLotUseCase creerLotUseCase;
    private final BloquerLotUseCase bloquerLotUseCase;
    private final DebloquerLotUseCase debloquerLotUseCase;
    private final GetLotUseCase getLotUseCase;
    private final ListLotsUseCase listLotsUseCase;
    private final ListerAlertesPeremptionUseCase listerAlertesPeremptionUseCase;
    private final ModifierPrixLotUseCase modifierPrixLotUseCase;

    // ── Stock ────────────────────────────────────────────────────────────
    private final EntreeStockUseCase entreeStockUseCase;
    private final SortirStockUseCase sortirStockUseCase;
    private final ReserverStockUseCase reserverStockUseCase;
    private final ReserverStockFefoUseCase reserverStockFefoUseCase;
    private final LibererReservationUseCase libererReservationUseCase;
    private final GetStockUseCase getStockUseCase;
    private final ListStocksUseCase listStocksUseCase;
    private final ListerAlertesRuptureUseCase listerAlertesRuptureUseCase;
    private final DefinirSeuilAlerteUseCase definirSeuilAlerteUseCase;

    // ── MouvementStock ───────────────────────────────────────────────────
    private final GetMouvementUseCase getMouvementUseCase;
    private final ListMouvementsUseCase listMouvementsUseCase;

    public StockFacade(
            CreerLotUseCase creerLotUseCase,
            BloquerLotUseCase bloquerLotUseCase,
            DebloquerLotUseCase debloquerLotUseCase,
            GetLotUseCase getLotUseCase,
            ListLotsUseCase listLotsUseCase,
            ListerAlertesPeremptionUseCase listerAlertesPeremptionUseCase,
            ModifierPrixLotUseCase modifierPrixLotUseCase,
            EntreeStockUseCase entreeStockUseCase,
            SortirStockUseCase sortirStockUseCase,
            ReserverStockUseCase reserverStockUseCase,
            ReserverStockFefoUseCase reserverStockFefoUseCase,
            LibererReservationUseCase libererReservationUseCase,
            GetStockUseCase getStockUseCase,
            ListStocksUseCase listStocksUseCase,
            ListerAlertesRuptureUseCase listerAlertesRuptureUseCase,
            DefinirSeuilAlerteUseCase definirSeuilAlerteUseCase,
            GetMouvementUseCase getMouvementUseCase,
            ListMouvementsUseCase listMouvementsUseCase) {
        this.creerLotUseCase = creerLotUseCase;
        this.bloquerLotUseCase = bloquerLotUseCase;
        this.debloquerLotUseCase = debloquerLotUseCase;
        this.getLotUseCase = getLotUseCase;
        this.listLotsUseCase = listLotsUseCase;
        this.listerAlertesPeremptionUseCase = listerAlertesPeremptionUseCase;
        this.modifierPrixLotUseCase = modifierPrixLotUseCase;
        this.entreeStockUseCase = entreeStockUseCase;
        this.sortirStockUseCase = sortirStockUseCase;
        this.reserverStockUseCase = reserverStockUseCase;
        this.reserverStockFefoUseCase = reserverStockFefoUseCase;
        this.libererReservationUseCase = libererReservationUseCase;
        this.getStockUseCase = getStockUseCase;
        this.listStocksUseCase = listStocksUseCase;
        this.listerAlertesRuptureUseCase = listerAlertesRuptureUseCase;
        this.definirSeuilAlerteUseCase = definirSeuilAlerteUseCase;
        this.getMouvementUseCase = getMouvementUseCase;
        this.listMouvementsUseCase = listMouvementsUseCase;
    }

    // ── Lot ──────────────────────────────────────────────────────────────

    public LotDetail creerLot(CreerLotCommand command) {
        return creerLotUseCase.creer(command);
    }

    public LotDetail bloquerLot(BloquerLotCommand command) {
        return bloquerLotUseCase.bloquer(command);
    }

    public LotDetail debloquerLot(DebloquerLotCommand command) {
        return debloquerLotUseCase.debloquer(command);
    }

    public LotDetail obtenirLot(GetLotQuery query) {
        return getLotUseCase.obtenir(query);
    }

    public LotPage listerLots(ListLotsQuery query) {
        return listLotsUseCase.lister(query);
    }

    public LotPage listerAlertesPeremption(AlertePeremptionQuery query) {
        return listerAlertesPeremptionUseCase.lister(query);
    }

    public LotDetail modifierPrixLot(ModifierPrixLotCommand command) {
        return modifierPrixLotUseCase.modifierPrix(command);
    }

    // ── Stock ────────────────────────────────────────────────────────────

    public StockDetail entrerStock(EntreeStockCommand command) {
        return entreeStockUseCase.entrer(command);
    }

    public StockDetail sortirStock(SortieStockCommand command) {
        return sortirStockUseCase.sortir(command);
    }

    public StockDetail reserverStock(ReserverStockCommand command) {
        return reserverStockUseCase.reserver(command);
    }

    public ReservationFefoResult reserverStockFefo(ReserverStockFefoCommand command) {
        return reserverStockFefoUseCase.reserver(command);
    }

    public StockDetail libererReservation(LibererReservationCommand command) {
        return libererReservationUseCase.liberer(command);
    }

    public StockDetail obtenirStock(GetStockQuery query) {
        return getStockUseCase.obtenir(query);
    }

    public StockPage listerStocks(ListStocksQuery query) {
        return listStocksUseCase.lister(query);
    }

    public StockPage listerAlertesRupture(ListStocksQuery query) {
        return listerAlertesRuptureUseCase.lister(query);
    }

    public StockDetail definirSeuilAlerte(DefinirSeuilAlerteCommand command) {
        return definirSeuilAlerteUseCase.definir(command);
    }

    // ── MouvementStock ───────────────────────────────────────────────────

    public MouvementDetail obtenirMouvement(GetMouvementQuery query) {
        return getMouvementUseCase.obtenir(query);
    }

    public MouvementPage listerMouvements(ListMouvementsQuery query) {
        return listMouvementsUseCase.lister(query);
    }
}
