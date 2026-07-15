package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class AccesFactureRefuseException extends SenPnaException {
    public AccesFactureRefuseException() {
        super("Vous n'êtes pas autorisé à accéder à cette facture", "FACTURE_ACCESS_DENIED", ErrorCategory.FORBIDDEN);
    }
}
