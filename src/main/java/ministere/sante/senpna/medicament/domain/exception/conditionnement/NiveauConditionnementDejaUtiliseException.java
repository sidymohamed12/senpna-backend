package ministere.sante.senpna.medicament.domain.exception.conditionnement;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsque le niveau (ordre d'emballage) est déjà occupé par un autre
 * conditionnement du même médicament.
 */
public class NiveauConditionnementDejaUtiliseException extends SenPnaException {
    public NiveauConditionnementDejaUtiliseException(int niveau) {
        super("Le niveau " + niveau + " est déjà utilisé par un autre conditionnement de ce médicament",
                "CONDITIONNEMENT_NIVEAU_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
