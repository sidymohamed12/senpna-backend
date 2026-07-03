package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

/**
 * Levée lorsqu'un médicament est créé ou modifié avec une famille
 * thérapeutique archivée — une famille retirée du référentiel actif ne
 * peut plus être rattachée à un nouveau médicament ni à une modification.
 */
public class FamilleInactiveException extends BusinessRuleException {
    public FamilleInactiveException() {
        super("La famille thérapeutique sélectionnée est archivée", "FAMILLE_INACTIVE");
    }
}
