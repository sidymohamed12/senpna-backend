package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class EntrepotInactifException extends SenPnaException {
    public EntrepotInactifException() {
        super("L'entrepôt est désactivé et ne peut pas recevoir de nouvelle affectation", "ENTREPOT_INACTIVE", ErrorCategory.BUSINESS_RULE);
    }
}
