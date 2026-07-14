package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class FournisseurIdRequisException extends SenPnaException {
    public FournisseurIdRequisException() {
        super("Un fournisseur est obligatoire pour le rôle FOURNISSEUR", "FOURNISSEUR_ID_REQUIRED", ErrorCategory.VALIDATION);
    }
}
