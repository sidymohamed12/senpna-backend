package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class EntrepotRequisException extends SenPnaException {
    public EntrepotRequisException() {
        super("Un entrepôt est obligatoire pour le(s) rôle(s) demandé(s)", "ENTREPOT_REQUIRED", ErrorCategory.VALIDATION);
    }
}
