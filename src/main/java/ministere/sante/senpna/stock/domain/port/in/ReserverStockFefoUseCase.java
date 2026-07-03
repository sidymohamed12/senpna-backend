package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.StockCommands.ReservationFefoResult;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockFefoCommand;

/**
 * Réservation automatique des quantités (cf. doc. métier §9) : applique la
 * règle FEFO en consommant les lots {@code ACTIF} non expirés du
 * médicament, par date d'expiration croissante, jusqu'à satisfaction de la
 * quantité demandée.
 */
public interface ReserverStockFefoUseCase {
    ReservationFefoResult reserver(ReserverStockFefoCommand command);
}
