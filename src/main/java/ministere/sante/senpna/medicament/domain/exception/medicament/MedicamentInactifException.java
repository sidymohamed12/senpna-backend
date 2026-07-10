package ministere.sante.senpna.medicament.domain.exception.medicament;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

/**
 * Levée lors de la création ou modification d'un conditionnement rattaché
 * à un médicament archivé.
 */
public class MedicamentInactifException extends BusinessRuleException {
    public MedicamentInactifException() {
        super("Le médicament est archivé — impossible de gérer ses conditionnements", "MEDICAMENT_INACTIVE");
    }
}
