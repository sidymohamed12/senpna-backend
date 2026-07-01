package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("Identifiant ou mot de passe incorrect", "INVALID_CREDENTIALS");
    }
}
