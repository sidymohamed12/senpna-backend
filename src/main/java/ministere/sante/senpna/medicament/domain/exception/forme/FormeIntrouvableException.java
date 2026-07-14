package ministere.sante.senpna.medicament.domain.exception.forme;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class FormeIntrouvableException extends SenPnaException {
    public FormeIntrouvableException() {
        super("Forme pharmaceutique introuvable", "FORME_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
