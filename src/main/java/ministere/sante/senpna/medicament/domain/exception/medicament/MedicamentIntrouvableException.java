package ministere.sante.senpna.medicament.domain.exception.medicament;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

public class MedicamentIntrouvableException extends SenPnaException {
    public MedicamentIntrouvableException() {
        super("Médicament introuvable", "MEDICAMENT_NOT_FOUND", ErrorCategory.NOT_FOUND);
    }
}
