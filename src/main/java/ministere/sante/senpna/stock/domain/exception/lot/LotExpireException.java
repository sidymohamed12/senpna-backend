package ministere.sante.senpna.stock.domain.exception.lot;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'une opération de changement de statut (blocage,
 * déblocage) est tentée sur un lot déjà {@code EXPIRE} — statut
 * terminal, cf. {@link ministere.sante.senpna.stock.domain.model.Lot}.
 */
public class LotExpireException extends SenPnaException {
    public LotExpireException(String numeroLot) {
        super("Le lot '" + numeroLot + "' est expiré (statut terminal) et ne peut plus être bloqué ni débloqué",
                "LOT_EXPIRED", ErrorCategory.BUSINESS_RULE);
    }
}
