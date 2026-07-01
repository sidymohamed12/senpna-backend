package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.UnauthorizedException;

public class ResetTokenInvalideException extends UnauthorizedException {
    public ResetTokenInvalideException() {
        super("Jeton de réinitialisation invalide ou expiré. Recommencez la procédure.", "RESET_TOKEN_INVALID");
    }
}
