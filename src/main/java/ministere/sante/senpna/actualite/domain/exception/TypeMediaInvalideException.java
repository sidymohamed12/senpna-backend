package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

public class TypeMediaInvalideException extends ValidationException {
    public TypeMediaInvalideException(String valeur) {
        super("Type de média invalide : '" + valeur + "' (attendu IMAGE ou VIDEO)", "TYPE_MEDIA_INVALIDE");
    }
}
