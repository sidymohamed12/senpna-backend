package ministere.sante.senpna.auth.domain.command;

import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;

import java.util.Set;
import java.util.UUID;

public final class AuthCommands {

    private AuthCommands() {
    }

    public record LoginCommand(String email, String password) {
    }

    public record ForgotPasswordCommand(String email, OtpChannel channel) {
    }

    public record ResendOtpCommand(String email, OtpChannel channel) {
    }

    public record VerifyOtpCommand(String email, String code) {
    }

    public record ResetPasswordCommand(String resetToken, String nouveauMotDePasse) {
    }

    public record RefreshTokenCommand(String refreshToken) {
    }

    public record MeQuery(String email) {
    }

    // ── Résultats ───────────────────────────────────────────────────────

    public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {
    }

    public record UserSummary(UUID id, String nom, String prenom, String email, Set<String> roles,
            UUID entrepotId, UUID structureSanitaireId) {
    }

    public record LoginResult(AuthTokens tokens, UserSummary user) {
    }

    public record VerifyOtpResult(String resetToken) {
    }
}
