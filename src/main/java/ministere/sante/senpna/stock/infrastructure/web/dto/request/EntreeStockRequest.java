package ministere.sante.senpna.stock.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record EntreeStockRequest(

                @NotNull(message = "L'entrepôt est obligatoire") UUID entrepotId,

                @NotNull(message = "Le lot est obligatoire") UUID lotId,

                @NotNull(message = "La quantité est obligatoire") @DecimalMin(value = "0.0001", message = "La quantité doit être strictement positive") BigDecimal quantite,

                @NotBlank(message = "Le type de mouvement est obligatoire") String typeMouvement,

                UUID commandeId,

                @Size(max = 100, message = "La référence document ne peut pas dépasser 100 caractères") String referenceDocument,

                @Size(max = 255, message = "Le motif ne peut pas dépasser 255 caractères") String motif) {
}
