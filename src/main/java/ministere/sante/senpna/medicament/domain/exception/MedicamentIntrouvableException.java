package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

public class MedicamentIntrouvableException extends NotFoundException {
    public MedicamentIntrouvableException() {
        super("Médicament introuvable", "MEDICAMENT_NOT_FOUND");
    }
}
