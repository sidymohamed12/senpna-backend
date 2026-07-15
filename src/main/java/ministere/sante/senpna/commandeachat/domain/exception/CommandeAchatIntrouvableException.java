package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CommandeAchatIntrouvableException extends SenPnaException {
    public CommandeAchatIntrouvableException() {
        super("Commande d'achat introuvable", "COMMANDE_ACHAT_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
