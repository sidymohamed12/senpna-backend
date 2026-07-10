package ministere.sante.senpna.medicament.domain.exception.conditionnement;

import ministere.sante.senpna.shared.domain.exception.ConflictException;

/**
 * Levée lorsque le niveau (ordre d'emballage) est déjà occupé par un autre
 * conditionnement du même médicament.
 */
public class NiveauConditionnementDejaUtiliseException extends ConflictException {
    public NiveauConditionnementDejaUtiliseException(int niveau) {
        super("Le niveau " + niveau + " est déjà utilisé par un autre conditionnement de ce médicament",
                "CONDITIONNEMENT_NIVEAU_ALREADY_USED");
    }
}
