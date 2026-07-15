package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record LigneCommandeAchatRequest(

        @NotNull(message = "Le médicament est obligatoire") UUID medicamentId,

        @NotNull(message = "Le conditionnement est obligatoire") UUID conditionnementId,

        @NotNull(message = "La quantité commandée est obligatoire") @DecimalMin(value = "0.01", message = "La quantité commandée doit être strictement positive") BigDecimal quantiteCommandee,

        @NotNull(message = "Le prix unitaire est obligatoire") @DecimalMin(value = "0.01", message = "Le prix unitaire doit être strictement positif") BigDecimal prixUnitaire) {
}
