package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.ConflictException;

public class EmailDejaUtiliseException extends ConflictException {
    public EmailDejaUtiliseException(String email) {
        super("Cet email est déjà utilisé par un autre utilisateur : " + email, "EMAIL_ALREADY_USED");
    }
}
