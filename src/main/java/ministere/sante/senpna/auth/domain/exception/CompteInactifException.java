package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CompteInactifException extends SenPnaException {
    public CompteInactifException() {
        super("Ce compte est désactivé. Contactez votre administrateur.", "ACCOUNT_INACTIVE", ErrorCategory.FORBIDDEN);
    }
}
