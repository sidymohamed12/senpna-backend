package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class StructureSanitaireIntrouvableException extends SenPnaException {
    public StructureSanitaireIntrouvableException() {
        super("Structure sanitaire introuvable", "STRUCTURE_SANITAIRE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
