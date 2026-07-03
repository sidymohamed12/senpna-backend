package ministere.sante.senpna.stock.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockResponse(
                UUID id,
                UUID entrepotId,
                UUID lotId,
                UUID medicamentId,
                BigDecimal quantiteDisponible,
                BigDecimal quantiteReservee,
                BigDecimal quantiteDisponibleALaVente,
                BigDecimal quantiteEnCommande,
                BigDecimal seuilAlerte,
                boolean enRupture,
                boolean seuilAtteint,
                Instant createdAt,
                Instant updatedAt) {
}
