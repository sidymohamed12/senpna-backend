package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InfoExpeditionLigneRequest(

        @NotNull(message = "La ligne de commande est obligatoire") UUID ligneId,

        @NotBlank(message = "Le numéro de lot est obligatoire") String numeroLot,

        LocalDate dateFabrication,

        @NotNull(message = "La date de péremption est obligatoire") LocalDate dateExpiration,

        String certificatAnalyseUrl,

        @NotNull(message = "La quantité expédiée est obligatoire") @DecimalMin(value = "0.01", message = "La quantité expédiée doit être strictement positive") BigDecimal quantiteExpediee) {
}
