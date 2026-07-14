package ministere.sante.senpna.stock.domain.exception.mouvement;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class MouvementStockIntrouvableException extends SenPnaException {
    public MouvementStockIntrouvableException() {
        super("Mouvement de stock introuvable", "MOUVEMENT_STOCK_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
