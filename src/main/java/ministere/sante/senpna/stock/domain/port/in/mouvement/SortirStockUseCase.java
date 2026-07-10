package ministere.sante.senpna.stock.domain.port.in.mouvement;

import ministere.sante.senpna.stock.domain.command.StockCommands.SortieStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;

/**
 * Sortie de stock (cf. doc. métier §9 et §13) : diminue la quantité
 * disponible d'une ligne de stock (perte, casse, vol, péremption,
 * ajustement, ou expédition d'une commande) et enregistre le mouvement
 * correspondant.
 */
public interface SortirStockUseCase {
    StockDetail sortir(SortieStockCommand command);
}
