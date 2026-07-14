package ministere.sante.senpna.medicament.domain.exception.medicament;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lors de la création ou modification d'un conditionnement rattaché
 * à un médicament archivé.
 */
public class MedicamentInactifException extends SenPnaException {
    public MedicamentInactifException() {
        super("Le médicament est archivé — impossible de gérer ses conditionnements", "MEDICAMENT_INACTIVE", ErrorCategory.BUSINESS_RULE);
    }
}
