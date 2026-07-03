package ministere.sante.senpna.stock.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record DefinirSeuilAlerteRequest(

                @DecimalMin(value = "0", message = "Le seuil d'alerte ne peut pas être négatif") BigDecimal seuilAlerte) {
}
