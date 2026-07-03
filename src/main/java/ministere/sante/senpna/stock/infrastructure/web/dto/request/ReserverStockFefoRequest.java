package ministere.sante.senpna.stock.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserverStockFefoRequest(

                @NotNull(message = "L'entrepôt est obligatoire") UUID entrepotId,

                @NotNull(message = "Le médicament est obligatoire") UUID medicamentId,

                @NotNull(message = "La quantité demandée est obligatoire") @DecimalMin(value = "0.0001", message = "La quantité doit être strictement positive") BigDecimal quantiteDemandee,

                UUID commandeId) {
}
