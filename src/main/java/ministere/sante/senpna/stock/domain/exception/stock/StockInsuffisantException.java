package ministere.sante.senpna.stock.domain.exception.stock;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

import java.math.BigDecimal;

/**
 * Levée lors d'une sortie ou d'une réservation dont la quantité demandée
 * dépasse la quantité disponible à la vente (cf. doc. métier §9 —
 * « Visualisation des alertes de rupture »).
 */
public class StockInsuffisantException extends SenPnaException {
    public StockInsuffisantException(BigDecimal disponible, BigDecimal demande) {
        super("Stock insuffisant : disponible=" + disponible + ", demandé=" + demande, "STOCK_INSUFFICIENT", ErrorCategory.BUSINESS_RULE);
    }
}
