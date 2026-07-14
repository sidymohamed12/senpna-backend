package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class StatutOpportuniteInvalideException extends SenPnaException {
    public StatutOpportuniteInvalideException(String valeur) {
        super("Statut d'opportunité invalide : '" + valeur
                + "' (valeurs acceptées : BROUILLON, OUVERT, EN_COURS, CLOTURE)",
                "STATUT_OPPORTUNITE_INVALIDE", ErrorCategory.VALIDATION);
    }
}
