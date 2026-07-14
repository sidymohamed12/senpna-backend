package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class TypeMediaInvalideException extends SenPnaException {
    public TypeMediaInvalideException(String valeur) {
        super("Type de média invalide : '" + valeur + "' (attendu IMAGE ou VIDEO)", "TYPE_MEDIA_INVALIDE", ErrorCategory.VALIDATION);
    }
}
