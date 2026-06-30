package ministere.sante.senpna.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Le jeton de réinitialisation est obligatoire") String resetToken,
        @NotBlank(message = "Le nouveau mot de passe est obligatoire") @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String nouveauMotDePasse) {
}
