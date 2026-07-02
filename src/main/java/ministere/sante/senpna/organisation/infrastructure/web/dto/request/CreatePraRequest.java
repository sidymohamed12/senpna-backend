package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreatePraRequest(

        @NotBlank(message = "Le code de la PRA est obligatoire")
        @Pattern(regexp = "^[A-Za-z0-9\\-]{2,30}$", message = "Le code doit être alphanumérique (2 à 30 caractères)")
        String code,

        @NotBlank(message = "Le nom de la PRA est obligatoire")
        String nom,

        @NotNull(message = "La région de rattachement est obligatoire")
        UUID regionId,

        String adresse,

        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le téléphone doit être au format international, ex: +221771234567")
        String telephone) {
}
