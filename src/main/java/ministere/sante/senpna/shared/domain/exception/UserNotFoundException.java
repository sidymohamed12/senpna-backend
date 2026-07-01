package ministere.sante.senpna.shared.domain.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("Utilisateur introuvable", "USER_NOT_FOUND");
    }
}
