package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class FactureIntrouvableException extends SenPnaException {
    public FactureIntrouvableException() {
        super("Facture introuvable", "FACTURE_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
