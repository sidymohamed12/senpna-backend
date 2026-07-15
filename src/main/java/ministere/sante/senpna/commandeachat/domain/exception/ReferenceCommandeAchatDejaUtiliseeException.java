package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class ReferenceCommandeAchatDejaUtiliseeException extends SenPnaException {
    public ReferenceCommandeAchatDejaUtiliseeException(String reference) {
        super("La référence de commande '" + reference + "' est déjà utilisée",
                "COMMANDE_ACHAT_REFERENCE_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
