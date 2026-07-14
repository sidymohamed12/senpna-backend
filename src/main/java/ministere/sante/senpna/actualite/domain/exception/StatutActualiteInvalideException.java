package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class StatutActualiteInvalideException extends SenPnaException {
    public StatutActualiteInvalideException(String valeur) {
        super("Statut d'actualité invalide : '" + valeur + "'", "STATUT_ACTUALITE_INVALIDE", ErrorCategory.VALIDATION);
    }
}
