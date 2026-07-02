package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateStructureSanitaireRequest(

        @NotBlank(message = "Le nom de la structure sanitaire est obligatoire")
        String nom,

        String district,

        String adresse,

        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le téléphone doit être au format international, ex: +221771234567")
        String telephone,

        @Email(message = "L'email doit être valide")
        String email,

        String responsableNom,

        String responsablePrenom) {
}
