package ministere.sante.senpna.commandeachat.domain.exception;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class TransitionStatutFactureInvalideException extends SenPnaException {
    public TransitionStatutFactureInvalideException(StatutFacture actuel, String actionDemandee) {
        super("Action '" + actionDemandee + "' impossible depuis le statut " + actuel,
                "FACTURE_TRANSITION_INVALID", ErrorCategory.BUSINESS_RULE);
    }
}
