package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class DernierRoleException extends SenPnaException {
    public DernierRoleException() {
        super("Impossible de retirer ce rôle : l'utilisateur doit posséder au moins un rôle actif",
                "LAST_ROLE_REQUIRED", ErrorCategory.BUSINESS_RULE);
    }
}
