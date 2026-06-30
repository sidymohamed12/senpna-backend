package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.ForbiddenException;

public class CompteInactifException extends ForbiddenException {
    public CompteInactifException() {
        super("Ce compte est désactivé. Contactez votre administrateur.", "ACCOUNT_INACTIVE");
    }
}
