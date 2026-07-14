package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class EntrepotIntrouvableException extends SenPnaException {
    public EntrepotIntrouvableException() {
        super("Entrepôt introuvable", "ENTREPOT_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
