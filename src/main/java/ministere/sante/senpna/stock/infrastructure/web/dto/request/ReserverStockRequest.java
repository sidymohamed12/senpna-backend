package ministere.sante.senpna.stock.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserverStockRequest(

                @NotNull(message = "L'entrepôt est obligatoire") UUID entrepotId,

                @NotNull(message = "Le lot est obligatoire") UUID lotId,

                @NotNull(message = "La quantité est obligatoire") @DecimalMin(value = "0.0001", message = "La quantité doit être strictement positive") BigDecimal quantite,

                UUID commandeId) {
}
