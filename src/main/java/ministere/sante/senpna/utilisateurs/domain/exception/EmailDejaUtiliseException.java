package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

public class EmailDejaUtiliseException extends ConflictException {
    public EmailDejaUtiliseException(String email) {
        super("Cet email est déjà utilisé par un autre utilisateur : " + email, "EMAIL_ALREADY_USED");
    }
}
