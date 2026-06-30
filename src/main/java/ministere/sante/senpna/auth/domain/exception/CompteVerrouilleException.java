package ministere.sante.senpna.auth.domain.exception;

import ministere.sante.senpna.shared.infrastructure.exception.ForbiddenException;

public class CompteVerrouilleException extends ForbiddenException {
    public CompteVerrouilleException() {
        super("Compte temporairement verrouillé suite à plusieurs échecs de connexion. Réessayez plus tard.",
                "ACCOUNT_LOCKED");
    }
}
