package ministere.sante.senpna.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
        @NotBlank(message = "Le code est obligatoire") @Pattern(regexp = "\\d{4,8}", message = "Code OTP invalide") String code) {
}
