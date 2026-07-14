package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class RoleNonAssigneException extends SenPnaException {
    public RoleNonAssigneException() {
        super("Ce rôle n'est pas attribué à cet utilisateur", "USER_ROLE_NOT_ASSIGNED", ErrorCategory.NOT_FOUND);
    }
}
