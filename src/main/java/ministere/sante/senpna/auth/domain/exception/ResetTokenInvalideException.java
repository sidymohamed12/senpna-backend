package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class ResetTokenInvalideException extends SenPnaException {
    public ResetTokenInvalideException() {
        super("Jeton de réinitialisation invalide ou expiré. Recommencez la procédure.", "RESET_TOKEN_INVALID", ErrorCategory.UNAUTHORIZED);
    }
}
