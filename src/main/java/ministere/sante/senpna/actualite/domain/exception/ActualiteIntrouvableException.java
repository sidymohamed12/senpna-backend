package ministere.sante.senpna.actualite.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class ActualiteIntrouvableException extends NotFoundException {
    public ActualiteIntrouvableException() {
        super("Actualité introuvable", "ACTUALITE_NOT_FOUND");
    }
}
