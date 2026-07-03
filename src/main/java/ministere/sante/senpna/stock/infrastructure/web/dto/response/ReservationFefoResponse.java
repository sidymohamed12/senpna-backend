package ministere.sante.senpna.stock.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReservationFefoResponse(
                UUID entrepotId,
                UUID medicamentId,
                BigDecimal quantiteDemandee,
                BigDecimal quantiteAllouee,
                boolean entierementSatisfaite,
                List<AllocationLotResponse> allocations) {
}
