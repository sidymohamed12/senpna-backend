package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class OpportuniteCarriereIntrouvableException extends SenPnaException {
    public OpportuniteCarriereIntrouvableException() {
        super("Opportunité de carrière introuvable", "OPPORTUNITE_CARRIERE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
