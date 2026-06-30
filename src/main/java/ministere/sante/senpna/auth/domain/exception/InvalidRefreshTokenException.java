package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.UnauthorizedException;

public class InvalidRefreshTokenException extends UnauthorizedException {
    public InvalidRefreshTokenException() {
        super("Refresh token invalide ou expiré", "INVALID_REFRESH_TOKEN");
    }
}
