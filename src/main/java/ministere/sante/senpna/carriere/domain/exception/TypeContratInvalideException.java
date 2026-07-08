package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class TypeContratInvalideException extends ValidationException {
    public TypeContratInvalideException(String valeur) {
        super("Type de contrat invalide : '" + valeur
                + "' (valeurs acceptées : CDI, CDD, STAGE, FREELANCE, VOLONTARIAT, AUTRE)",
                "TYPE_CONTRAT_INVALIDE");
    }
}
