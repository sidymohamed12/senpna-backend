package ministere.sante.senpna.medicament.domain.exception.conditionnement;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class ConditionnementIntrouvableException extends SenPnaException {
    public ConditionnementIntrouvableException() {
        super("Conditionnement introuvable", "CONDITIONNEMENT_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
