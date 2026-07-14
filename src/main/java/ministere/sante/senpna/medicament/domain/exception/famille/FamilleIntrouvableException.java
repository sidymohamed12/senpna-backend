package ministere.sante.senpna.medicament.domain.exception.famille;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class FamilleIntrouvableException extends SenPnaException {
    public FamilleIntrouvableException() {
        super("Famille thérapeutique introuvable", "FAMILLE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
