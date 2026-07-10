package ministere.sante.senpna.stock.domain.port.in.mouvement;

import ministere.sante.senpna.stock.domain.command.StockCommands.EntreeStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;

/**
 * Entrée en stock : augmente la quantité
 * disponible de la ligne de stock (entrepôt, lot) — créée si elle
 * n'existe pas encore — et enregistre le mouvement correspondant.
 */
public interface EntreeStockUseCase {
    StockDetail entrer(EntreeStockCommand command);
}
