package ministere.sante.senpna.organisation.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;

public record CreateStructureSanitaireRequest(

        @NotBlank(message = "Le code de la structure sanitaire est obligatoire")
        @Pattern(regexp = "^[A-Za-z0-9\\-]{2,30}$", message = "Le code doit être alphanumérique (2 à 30 caractères)")
        String code,

        @NotBlank(message = "Le nom de la structure sanitaire est obligatoire")
        String nom,

        @NotNull(message = "Le type de structure sanitaire est obligatoire")
        TypeStructureSanitaire type,

        String district,

        String adresse,

        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le téléphone doit être au format international, ex: +221771234567")
        String telephone,

        @Email(message = "L'email doit être valide")
        String email,

        String responsable) {
}
