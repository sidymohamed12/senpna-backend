package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class TransitionStatutOffreInvalideException extends SenPnaException {
    public TransitionStatutOffreInvalideException(StatutOffre actuel, String actionDemandee) {
        super("Action '" + actionDemandee + "' impossible depuis le statut " + actuel,
                "OFFRE_FOURNISSEUR_TRANSITION_INVALID");
    }
}
