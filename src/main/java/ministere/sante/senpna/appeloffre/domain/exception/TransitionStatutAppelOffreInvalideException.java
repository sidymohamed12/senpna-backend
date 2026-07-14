package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;

public class TransitionStatutAppelOffreInvalideException extends SenPnaException {
    public TransitionStatutAppelOffreInvalideException(StatutAppelOffre actuel, String actionDemandee) {
        super("Action '" + actionDemandee + "' impossible depuis le statut " + actuel,
                "APPEL_OFFRE_TRANSITION_INVALID");
    }
}
