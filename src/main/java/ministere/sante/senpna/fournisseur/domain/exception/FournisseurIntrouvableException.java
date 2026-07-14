package ministere.sante.senpna.fournisseur.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class FournisseurIntrouvableException extends SenPnaException {
    public FournisseurIntrouvableException() {
        super("Fournisseur introuvable", "FOURNISSEUR_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
