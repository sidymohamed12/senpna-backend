package ministere.sante.senpna.catalogue.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

/**
 * Levée lorsque le catalogue régional est demandé pour une région ne
 * possédant aucun entrepôt PRA actif — la structure sanitaire (ou
 * l'acteur PNA consultant pour supervision) ne peut alors se voir
 * présenter aucune disponibilité.
 */
public class AucunePraPourRegionException extends NotFoundException {

    public AucunePraPourRegionException() {
        super("Aucune PRA active n'est rattachée à cette région", "PRA_NOT_FOUND_FOR_REGION");
    }
}
