package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

public class DernierRoleException extends BusinessRuleException {
    public DernierRoleException() {
        super("Impossible de retirer ce rôle : l'utilisateur doit posséder au moins un rôle actif",
                "LAST_ROLE_REQUIRED");
    }
}
