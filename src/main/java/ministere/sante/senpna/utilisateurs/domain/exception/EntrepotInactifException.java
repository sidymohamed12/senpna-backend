package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

public class EntrepotInactifException extends BusinessRuleException {
    public EntrepotInactifException() {
        super("L'entrepôt est désactivé et ne peut pas recevoir de nouvelle affectation", "ENTREPOT_INACTIVE");
    }
}
