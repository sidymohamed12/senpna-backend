package ministere.sante.senpna.appeloffre.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Un fournisseur ne peut déposer qu'une seule offre active (non retirée)
 * par appel d'offres — une nouvelle soumission doit d'abord passer par le
 * retrait de la précédente.
 */
public class OffreDejaSoumiseException extends SenPnaException {
    public OffreDejaSoumiseException() {
        super("Une offre a déjà été soumise par ce fournisseur pour cet appel d'offres",
                "OFFRE_FOURNISSEUR_ALREADY_SUBMITTED", ErrorCategory.CONFLICT);
    }
}
