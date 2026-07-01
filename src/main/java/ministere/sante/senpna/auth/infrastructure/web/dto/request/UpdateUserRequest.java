package ministere.sante.senpna.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Le téléphone doit être au format international, ex: +221771234567")
        String telephone) {
}
