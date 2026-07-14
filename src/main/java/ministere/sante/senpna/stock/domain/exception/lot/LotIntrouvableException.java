package ministere.sante.senpna.stock.domain.exception.lot;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class LotIntrouvableException extends SenPnaException {
    public LotIntrouvableException() {
        super("Lot introuvable", "LOT_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
