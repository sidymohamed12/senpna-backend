package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("Utilisateur introuvable", "USER_NOT_FOUND");
    }
}
