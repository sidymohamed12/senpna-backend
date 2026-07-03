package ministere.sante.senpna.stock.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class MouvementStockIntrouvableException extends NotFoundException {
    public MouvementStockIntrouvableException() {
        super("Mouvement de stock introuvable", "MOUVEMENT_STOCK_NOT_FOUND");
    }
}
