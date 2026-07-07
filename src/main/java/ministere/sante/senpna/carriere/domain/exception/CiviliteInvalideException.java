package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class CiviliteInvalideException extends ValidationException {
    public CiviliteInvalideException(String valeur) {
        super("Civilité invalide : '" + valeur + "' (valeurs acceptées : M, MME)", "CIVILITE_INVALIDE");
    }
}
