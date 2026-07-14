package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class OffreFournisseurIntrouvableException extends SenPnaException {
    public OffreFournisseurIntrouvableException() {
        super("Offre introuvable", "OFFRE_FOURNISSEUR_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
