package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class EntrepotNonApplicableException extends SenPnaException {
    public EntrepotNonApplicableException() {
        super("Aucun entrepôt n'est attendu pour le(s) rôle(s) demandé(s)", "ENTREPOT_NOT_APPLICABLE", ErrorCategory.VALIDATION);
    }
}
