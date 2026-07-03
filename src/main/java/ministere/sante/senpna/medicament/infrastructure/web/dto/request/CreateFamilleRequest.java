package ministere.sante.senpna.medicament.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFamilleRequest(

        @NotBlank(message = "Le code de la famille est obligatoire") @Pattern(regexp = "^[A-Za-z0-9_-]{2,30}$", message = "Le code doit être alphanumérique (2 à 30 caractères)") String code,

        @NotBlank(message = "Le libellé de la famille est obligatoire") @Size(max = 150, message = "Le libellé ne peut pas dépasser 150 caractères") String libelle,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères") String description) {
}
