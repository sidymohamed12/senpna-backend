package ministere.sante.senpna.medicament.domain.exception.forme;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class FormeIntrouvableException extends NotFoundException {
    public FormeIntrouvableException() {
        super("Forme pharmaceutique introuvable", "FORME_NOT_FOUND");
    }
}
