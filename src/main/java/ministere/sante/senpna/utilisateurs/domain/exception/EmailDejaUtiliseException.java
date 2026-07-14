package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class EmailDejaUtiliseException extends SenPnaException {
    public EmailDejaUtiliseException(String email) {
        super("Cet email est déjà utilisé par un autre utilisateur : " + email, "EMAIL_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
