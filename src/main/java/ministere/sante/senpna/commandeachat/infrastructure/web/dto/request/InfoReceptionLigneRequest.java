package ministere.sante.senpna.commandeachat.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record InfoReceptionLigneRequest(

        @NotNull(message = "La ligne de commande est obligatoire") UUID ligneId,

        @DecimalMin(value = "0.0", message = "La quantité reçue ne peut pas être négative") BigDecimal quantiteRecue,

        @DecimalMin(value = "0.0", message = "La quantité refusée ne peut pas être négative") BigDecimal quantiteRefusee,

        @Size(max = 500, message = "Le motif de refus ne peut pas dépasser 500 caractères") String motifRefus) {
}
