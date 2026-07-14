package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class ActualiteIntrouvableException extends SenPnaException {
    public ActualiteIntrouvableException() {
        super("Actualité introuvable", "ACTUALITE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
