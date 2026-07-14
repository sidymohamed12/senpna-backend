package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class DateLimiteCandidatureInvalideException extends SenPnaException {
    public DateLimiteCandidatureInvalideException(String message) {
        super(message, "DATE_LIMITE_CANDIDATURE_INVALIDE", ErrorCategory.VALIDATION);
    }
}
