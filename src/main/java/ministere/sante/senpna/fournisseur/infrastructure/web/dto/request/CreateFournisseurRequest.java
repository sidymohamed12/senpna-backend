package ministere.sante.senpna.fournisseur.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFournisseurRequest(

                @NotBlank(message = "Le nom du fournisseur est obligatoire") @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères") String nom,

                @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères") String adresse,

                @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le téléphone doit être au format international, ex: +221771234567") String telephone,

                @Email(message = "L'email doit être valide") @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères") String email,

                @Size(max = 150, message = "Le contact principal ne peut pas dépasser 150 caractères") String contactPrincipal) {
}
