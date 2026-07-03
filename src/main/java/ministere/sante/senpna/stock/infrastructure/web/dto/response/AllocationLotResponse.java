package ministere.sante.senpna.stock.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocationLotResponse(UUID lotId, String numeroLot, BigDecimal quantiteAllouee) {
}
