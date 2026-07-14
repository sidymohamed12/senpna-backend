package ministere.sante.senpna.projet.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class ProjetIntrouvableException extends SenPnaException {
    public ProjetIntrouvableException() {
        super("Projet introuvable", "PROJET_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
