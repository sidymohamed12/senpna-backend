package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.NotFoundException;

public class RoleIntrouvableException extends NotFoundException {
    public RoleIntrouvableException() {
        super("Rôle introuvable", "ROLE_NOT_FOUND");
    }
}
