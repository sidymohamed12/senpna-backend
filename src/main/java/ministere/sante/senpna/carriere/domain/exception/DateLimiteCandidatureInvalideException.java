package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class DateLimiteCandidatureInvalideException extends ValidationException {
    public DateLimiteCandidatureInvalideException(String message) {
        super(message, "DATE_LIMITE_CANDIDATURE_INVALIDE");
    }
}
