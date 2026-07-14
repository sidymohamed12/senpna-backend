package ministere.sante.senpna.stock.domain.exception.stock;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class StockIntrouvableException extends SenPnaException {
    public StockIntrouvableException() {
        super("Ligne de stock introuvable", "STOCK_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
