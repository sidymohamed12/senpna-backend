package ministere.sante.senpna.medicament.domain.exception.famille;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'un médicament est créé ou modifié avec une famille
 * thérapeutique archivée — une famille retirée du référentiel actif ne
 * peut plus être rattachée à un nouveau médicament ni à une modification.
 */
public class FamilleInactiveException extends SenPnaException {
    public FamilleInactiveException() {
        super("La famille thérapeutique sélectionnée est archivée", "FAMILLE_INACTIVE", ErrorCategory.BUSINESS_RULE);
    }
}
