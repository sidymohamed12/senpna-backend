package ministere.sante.senpna.stock.domain.exception.mouvement;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsque la combinaison type/sens/entrepôts d'un mouvement de stock
 * viole les règles structurelles du modèle métier complémentaire §2
 * « Mouvements de stock » (ex : une sortie sans entrepôt source, un
 * transfert sans entrepôt destination).
 */
public class MouvementStockInvalideException extends SenPnaException {
    public MouvementStockInvalideException(String message) {
        super(message, "MOUVEMENT_STOCK_INVALID", ErrorCategory.VALIDATION);
    }
}
