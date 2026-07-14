package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class InvalidCredentialsException extends SenPnaException {
    public InvalidCredentialsException() {
        super("Identifiant ou mot de passe incorrect", "INVALID_CREDENTIALS", ErrorCategory.UNAUTHORIZED);
    }
}
