package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class RoleDejaAssigneException extends SenPnaException {
    public RoleDejaAssigneException() {
        super("Ce rôle est déjà attribué à cet utilisateur", "ROLE_ALREADY_ASSIGNED", ErrorCategory.CONFLICT);
    }
}
