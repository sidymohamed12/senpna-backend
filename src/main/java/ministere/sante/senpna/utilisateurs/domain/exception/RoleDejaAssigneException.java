package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.ConflictException;

public class RoleDejaAssigneException extends ConflictException {
    public RoleDejaAssigneException() {
        super("Ce rôle est déjà attribué à cet utilisateur", "ROLE_ALREADY_ASSIGNED");
    }
}
