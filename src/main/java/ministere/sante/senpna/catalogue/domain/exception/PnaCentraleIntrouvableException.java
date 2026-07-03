package ministere.sante.senpna.catalogue.domain.exception;

import ministere.sante.senpna.shared.domain.exception.NotFoundException;

/**
 * Levée lorsque le catalogue PNA est demandé alors qu'aucun entrepôt actif
 * de type {@code PNA_CENTRAL} n'est configuré — situation anormale
 * puisque la PNA centrale est une donnée de référence unique provisionnée
 * par migration (cf. Javadoc {@code Entrepot}), mais traitée
 * défensivement plutôt que de propager une erreur technique opaque.
 */
public class PnaCentraleIntrouvableException extends NotFoundException {

    public PnaCentraleIntrouvableException() {
        super("Aucun entrepôt PNA centrale actif n'est configuré", "PNA_CENTRALE_NOT_CONFIGURED");
    }
}
