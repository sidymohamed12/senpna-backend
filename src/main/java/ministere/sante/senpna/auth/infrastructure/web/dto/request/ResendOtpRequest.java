package ministere.sante.senpna.auth.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

public record ResendOtpRequest(
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
        @NotNull(message = "Le canal (SMS ou EMAIL) est obligatoire") OtpChannel channel) {
}
