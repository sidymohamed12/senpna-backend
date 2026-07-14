package ministere.sante.senpna.shared.domain.exception;

public class UserNotFoundException extends SenPnaException {
    public UserNotFoundException() {
        super("Utilisateur introuvable", "USER_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
