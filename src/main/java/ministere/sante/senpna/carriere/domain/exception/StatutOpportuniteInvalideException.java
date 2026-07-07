package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class StatutOpportuniteInvalideException extends ValidationException {
    public StatutOpportuniteInvalideException(String valeur) {
        super("Statut d'opportunité invalide : '" + valeur
                + "' (valeurs acceptées : BROUILLON, OUVERT, EN_COURS, CLOTURE)",
                "STATUT_OPPORTUNITE_INVALIDE");
    }
}
