package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class TransitionStatutCommandeAchatInvalideException extends SenPnaException {
    public TransitionStatutCommandeAchatInvalideException(StatutCommandeAchat actuel, String actionDemandee) {
        super("Action '" + actionDemandee + "' impossible depuis le statut " + actuel,
                "COMMANDE_ACHAT_TRANSITION_INVALID", ErrorCategory.BUSINESS_RULE);
    }
}
