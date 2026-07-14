package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class RoleIntrouvableException extends SenPnaException {
    public RoleIntrouvableException() {
        super("Rôle introuvable", "ROLE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
