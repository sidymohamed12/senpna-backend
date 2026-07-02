package ministere.sante.senpna.organisation.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

public class StructureSanitaireNonValideeException extends BusinessRuleException {
    public StructureSanitaireNonValideeException() {
        super("La structure sanitaire ne peut être activée qu'après validation de son adhésion",
                "STRUCTURE_SANITAIRE_ADHESION_NOT_VALIDATED");
    }
}
