package ministere.sante.senpna.stock.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class StockIntrouvableException extends NotFoundException {
    public StockIntrouvableException() {
        super("Ligne de stock introuvable", "STOCK_NOT_FOUND");
    }
}
