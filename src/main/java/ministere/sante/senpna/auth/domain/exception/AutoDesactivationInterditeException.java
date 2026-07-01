package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.BusinessRuleException;

public class AutoDesactivationInterditeException extends BusinessRuleException {
    public AutoDesactivationInterditeException() {
        super("Vous ne pouvez pas désactiver votre propre compte", "SELF_DEACTIVATION_FORBIDDEN");
    }
}
