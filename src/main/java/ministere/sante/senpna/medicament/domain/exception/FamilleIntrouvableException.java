package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class FamilleIntrouvableException extends NotFoundException {
    public FamilleIntrouvableException() {
        super("Famille thérapeutique introuvable", "FAMILLE_NOT_FOUND");
    }
}
