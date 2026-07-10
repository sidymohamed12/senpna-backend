package ministere.sante.senpna.medicament.domain.exception.forme;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

/**
 * Levée lorsqu'un médicament est créé ou modifié avec une forme
 * pharmaceutique archivée.
 */
public class FormeInactiveException extends BusinessRuleException {
    public FormeInactiveException() {
        super("La forme pharmaceutique sélectionnée est archivée", "FORME_INACTIVE");
    }
}
