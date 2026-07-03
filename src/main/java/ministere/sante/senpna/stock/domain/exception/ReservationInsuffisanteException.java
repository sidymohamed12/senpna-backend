package ministere.sante.senpna.stock.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

import java.math.BigDecimal;

/**
 * Levée lorsqu'on tente de libérer ou de consommer (sortie depuis
 * réservation) une quantité supérieure à celle effectivement réservée.
 */
public class ReservationInsuffisanteException extends BusinessRuleException {
    public ReservationInsuffisanteException(BigDecimal reservee, BigDecimal demande) {
        super("Quantité réservée insuffisante : réservée=" + reservee + ", demandée=" + demande,
                "RESERVATION_INSUFFICIENT");
    }
}
