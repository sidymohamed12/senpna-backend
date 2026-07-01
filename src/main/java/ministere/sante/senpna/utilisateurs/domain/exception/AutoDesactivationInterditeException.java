package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

public class AutoDesactivationInterditeException extends BusinessRuleException {
    public AutoDesactivationInterditeException() {
        super("Vous ne pouvez pas désactiver votre propre compte", "SELF_DEACTIVATION_FORBIDDEN");
    }
}
