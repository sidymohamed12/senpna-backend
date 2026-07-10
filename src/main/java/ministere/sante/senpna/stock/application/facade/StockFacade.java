package ministere.sante.senpna.stock.application.facade;

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
import ministere.sante.senpna.stock.domain.port.in.mouvement.EntreeStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.mouvement.SortirStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.DefinirSeuilAlerteUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.GetStockUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.LibererReservationUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ListStocksUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ListerAlertesRuptureUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ReserverStockFefoUseCase;
import ministere.sante.senpna.stock.domain.port.in.stock.ReserverStockUseCase;

import org.springframework.stereotype.Component;

@Component
public class StockFacade {

    private final EntreeStockUseCase entreeStockUseCase;
    private final SortirStockUseCase sortirStockUseCase;
    private final ReserverStockUseCase reserverStockUseCase;
    private final ReserverStockFefoUseCase reserverStockFefoUseCase;
    private final LibererReservationUseCase libererReservationUseCase;
    private final GetStockUseCase getStockUseCase;
    private final ListStocksUseCase listStocksUseCase;
    private final ListerAlertesRuptureUseCase listerAlertesRuptureUseCase;
    private final DefinirSeuilAlerteUseCase definirSeuilAlerteUseCase;

    public StockFacade(EntreeStockUseCase entreeStockUseCase, SortirStockUseCase sortirStockUseCase,
            ReserverStockUseCase reserverStockUseCase, ReserverStockFefoUseCase reserverStockFefoUseCase,
            LibererReservationUseCase libererReservationUseCase, GetStockUseCase getStockUseCase,
            ListStocksUseCase listStocksUseCase, ListerAlertesRuptureUseCase listerAlertesRuptureUseCase,
            DefinirSeuilAlerteUseCase definirSeuilAlerteUseCase) {
        this.entreeStockUseCase = entreeStockUseCase;
        this.sortirStockUseCase = sortirStockUseCase;
        this.reserverStockUseCase = reserverStockUseCase;
        this.reserverStockFefoUseCase = reserverStockFefoUseCase;
        this.libererReservationUseCase = libererReservationUseCase;
        this.getStockUseCase = getStockUseCase;
        this.listStocksUseCase = listStocksUseCase;
        this.listerAlertesRuptureUseCase = listerAlertesRuptureUseCase;
        this.definirSeuilAlerteUseCase = definirSeuilAlerteUseCase;
    }

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

}
