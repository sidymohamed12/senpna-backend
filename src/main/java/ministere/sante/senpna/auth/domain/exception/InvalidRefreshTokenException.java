package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class InvalidRefreshTokenException extends SenPnaException {
    public InvalidRefreshTokenException() {
        super("Refresh token invalide ou expiré", "INVALID_REFRESH_TOKEN", ErrorCategory.UNAUTHORIZED);
    }
}
