package ministere.sante.senpna.stock.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class LotIntrouvableException extends NotFoundException {
    public LotIntrouvableException() {
        super("Lot introuvable", "LOT_NOT_FOUND");
    }
}
