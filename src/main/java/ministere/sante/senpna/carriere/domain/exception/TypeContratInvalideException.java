package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class TypeContratInvalideException extends SenPnaException {
    public TypeContratInvalideException(String valeur) {
        super("Type de contrat invalide : '" + valeur
                + "' (valeurs acceptées : CDI, CDD, STAGE, FREELANCE, VOLONTARIAT, AUTRE)",
                "TYPE_CONTRAT_INVALIDE", ErrorCategory.VALIDATION);
    }
}
