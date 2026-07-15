package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class LigneCommandeAchatIntrouvableException extends SenPnaException {
    public LigneCommandeAchatIntrouvableException() {
        super("Ligne de commande introuvable", "LIGNE_COMMANDE_ACHAT_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
