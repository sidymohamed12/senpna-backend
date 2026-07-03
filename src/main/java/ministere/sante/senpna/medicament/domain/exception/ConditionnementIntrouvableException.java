package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class ConditionnementIntrouvableException extends NotFoundException {
    public ConditionnementIntrouvableException() {
        super("Conditionnement introuvable", "CONDITIONNEMENT_NOT_FOUND");
    }
}
