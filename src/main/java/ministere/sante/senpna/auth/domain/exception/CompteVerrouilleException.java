package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CompteVerrouilleException extends SenPnaException {
    public CompteVerrouilleException() {
        super("Compte temporairement verrouillé suite à plusieurs échecs de connexion. Réessayez plus tard.",
                "ACCOUNT_LOCKED", ErrorCategory.FORBIDDEN);
    }
}
