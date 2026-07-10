package ministere.sante.senpna.stock.domain.port.in.stock;

import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;

/**
 * Réservation manuelle ciblée sur une ligne de stock (entrepôt, lot) déjà
 * identifiée.
 */
public interface ReserverStockUseCase {
    StockDetail reserver(ReserverStockCommand command);
}
