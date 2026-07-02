package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateRegionRequest(

        @NotBlank(message = "Le code de la région est obligatoire")
        @Pattern(regexp = "^[A-Za-z0-9\\-]{2,20}$", message = "Le code doit être alphanumérique (2 à 20 caractères)")
        String code,

        @NotBlank(message = "Le nom de la région est obligatoire")
        String nom) {
}
