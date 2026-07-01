package ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.Set;
import java.util.UUID;

public record CreateUserRequest(

                @NotBlank(message = "Le nom est obligatoire") String nom,

                @NotBlank(message = "Le prénom est obligatoire") String prenom,

                @NotBlank(message = "L'email est obligatoire") @Email(message = "L'email doit être valide") String email,

                @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Le téléphone doit être au format international, ex: +221771234567") String telephone,

                @NotEmpty(message = "Au moins un rôle doit être attribué") Set<UUID> roleIds) {
}
