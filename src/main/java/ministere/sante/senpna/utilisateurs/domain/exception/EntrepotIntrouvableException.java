package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class EntrepotIntrouvableException extends NotFoundException {
    public EntrepotIntrouvableException() {
        super("Entrepôt introuvable", "ENTREPOT_NOT_FOUND");
    }
}
