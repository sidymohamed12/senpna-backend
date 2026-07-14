package ministere.sante.senpna.medicament.domain.exception.conditionnement;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsque le nom d'un conditionnement (ex: "Carton") est déjà utilisé
 * par un autre conditionnement du même médicament — l'unicité du nom est
 * évaluée par médicament, pas globalement.
 */
public class NomConditionnementDejaUtiliseException extends SenPnaException {
    public NomConditionnementDejaUtiliseException(String nom) {
        super("Un conditionnement nommé '" + nom + "' existe déjà pour ce médicament",
                "CONDITIONNEMENT_NOM_ALREADY_USED", ErrorCategory.CONFLICT);
    }
}
