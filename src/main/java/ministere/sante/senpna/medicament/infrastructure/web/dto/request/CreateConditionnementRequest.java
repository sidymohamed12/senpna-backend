package ministere.sante.senpna.medicament.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateConditionnementRequest(

        @NotNull(message = "Le médicament est obligatoire") UUID medicamentId,

        @NotBlank(message = "Le nom du conditionnement est obligatoire") @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères") String nom,

        @Min(value = 1, message = "Le niveau doit être supérieur ou égal à 1") int niveau,

        @NotNull(message = "La quantité en unité de base est obligatoire") @DecimalMin(value = "0.0001", message = "La quantité en unité de base doit être strictement positive") BigDecimal quantiteUniteBase,

        boolean estUniteBase) {
}
