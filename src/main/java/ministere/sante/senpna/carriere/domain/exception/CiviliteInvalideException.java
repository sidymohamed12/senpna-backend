package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class CiviliteInvalideException extends SenPnaException {
    public CiviliteInvalideException(String valeur) {
        super("Civilité invalide : '" + valeur + "' (valeurs acceptées : M, MME)", "CIVILITE_INVALIDE", ErrorCategory.VALIDATION);
    }
}
