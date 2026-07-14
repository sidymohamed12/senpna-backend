package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class AutoDesactivationInterditeException extends SenPnaException {
    public AutoDesactivationInterditeException() {
        super("Vous ne pouvez pas désactiver votre propre compte", "SELF_DEACTIVATION_FORBIDDEN", ErrorCategory.BUSINESS_RULE);
    }
}
