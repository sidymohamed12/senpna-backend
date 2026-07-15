package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un fournisseur tente d'accéder à ou d'agir sur une
 * commande d'achat qui ne lui est pas destinée.
 */
public class AccesCommandeAchatRefuseException extends SenPnaException {
    public AccesCommandeAchatRefuseException() {
        super("Vous n'êtes pas autorisé à accéder à cette commande", "COMMANDE_ACHAT_ACCESS_DENIED", ErrorCategory.FORBIDDEN);
    }
}
