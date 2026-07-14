package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class FournisseurIdRequisException extends ValidationException {
    public FournisseurIdRequisException() {
        super("Un fournisseur est obligatoire pour le rôle FOURNISSEUR", "FOURNISSEUR_ID_REQUIRED");
    }
}
