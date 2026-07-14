package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CandidatureIntrouvableException extends SenPnaException {
    public CandidatureIntrouvableException() {
        super("Candidature introuvable", "CANDIDATURE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
