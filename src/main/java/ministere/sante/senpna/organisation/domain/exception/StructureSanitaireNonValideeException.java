package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class StructureSanitaireNonValideeException extends SenPnaException {
    public StructureSanitaireNonValideeException() {
        super("La structure sanitaire ne peut être activée qu'après validation de son adhésion",
                "STRUCTURE_SANITAIRE_ADHESION_NOT_VALIDATED", ErrorCategory.BUSINESS_RULE);
    }
}
