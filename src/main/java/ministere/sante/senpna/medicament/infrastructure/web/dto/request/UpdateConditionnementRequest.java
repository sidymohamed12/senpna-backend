package ministere.sante.senpna.medicament.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateConditionnementRequest(

                @NotBlank(message = "Le nom du conditionnement est obligatoire") @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères") String nom,

                @Min(value = 1, message = "Le niveau doit être supérieur ou égal à 1") int niveau,

                @NotNull(message = "La quantité en unité de base est obligatoire") @DecimalMin(value = "0.0001", message = "La quantité en unité de base doit être strictement positive") BigDecimal quantiteUniteBase,

                boolean estUniteBase,

                @DecimalMin(value = "0.0", message = "Le prix d'achat ne peut pas être négatif") BigDecimal prixAchat,

                @DecimalMin(value = "0.0", message = "Le prix de vente ne peut pas être négatif") BigDecimal prixVente) {
}
