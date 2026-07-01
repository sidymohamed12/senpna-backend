package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class RoleNonAssigneException extends NotFoundException {
    public RoleNonAssigneException() {
        super("Ce rôle n'est pas attribué à cet utilisateur", "USER_ROLE_NOT_ASSIGNED");
    }
}
