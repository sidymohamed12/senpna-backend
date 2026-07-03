package ministere.sante.senpna.stock.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ModifierPrixLotRequest(

                @DecimalMin(value = "0", message = "Le prix d'achat ne peut pas être négatif") BigDecimal prixAchat,

                @DecimalMin(value = "0", message = "Le prix de vente ne peut pas être négatif") BigDecimal prixVente) {
}
