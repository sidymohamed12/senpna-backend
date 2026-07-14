package ministere.sante.senpna.stock.domain.exception.lot;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'une réservation ou une sortie de stock est tentée sur un lot
 * bloqué ou expiré (cf. modèle métier complémentaire §2 « Lots » : « Les
 * lots expirés ne peuvent pas être réservés / expédiés »).
 */
public class LotNonDisponibleException extends SenPnaException {
    public LotNonDisponibleException(String numeroLot) {
        super("Le lot '" + numeroLot + "' n'est pas disponible (bloqué ou expiré)", "LOT_NOT_AVAILABLE", ErrorCategory.BUSINESS_RULE);
    }
}
