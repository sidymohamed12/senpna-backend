package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record UpdatePraRequest(

        @NotBlank(message = "Le nom de la PRA est obligatoire")
        String nom,

        String adresse,

        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le téléphone doit être au format international, ex: +221771234567")
        String telephone,

        UUID regionId) {
}
