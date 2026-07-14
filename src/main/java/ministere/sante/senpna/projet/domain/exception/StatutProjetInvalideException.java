package ministere.sante.senpna.projet.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class StatutProjetInvalideException extends SenPnaException {
    public StatutProjetInvalideException(String valeur) {
        super("Statut de projet invalide : '" + valeur + "'", "STATUT_PROJET_INVALIDE", ErrorCategory.VALIDATION);
    }
}
