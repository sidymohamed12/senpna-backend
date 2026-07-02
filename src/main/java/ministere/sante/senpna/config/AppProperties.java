package ministere.sante.senpna.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
                JwtProperties jwt,
                SecurityProperties security,
                OtpProperties otp,
                RateLimitProperties rateLimit,
                MailProperties mail) {

        // ── JWT ───────────────────────────────────────────────────────────────

        public record JwtProperties(
                        String secret,
                        @DefaultValue("PT15M") Duration accessTokenTtl,
                        @DefaultValue("P7D") Duration refreshTokenTtl) {
        }

        // ── Sécurité ─────────────────────────────────────────────────────────

        public record SecurityProperties(
                        @DefaultValue("http://localhost:4200") java.util.List<String> corsAllowedOrigins) {
        }

        // ── OTP ───────────────────────────────────────────────────────────────

        public record OtpProperties(
                        @DefaultValue("6") int length,
                        @DefaultValue("PT10M") Duration ttl,
                        @DefaultValue("PT2M") Duration cooldown,
                        @DefaultValue("3") int maxAttempts) {
        }

        // ── Rate Limit ────────────────────────────────────────────────────────

        /**
         * Configuration du rate limiting.
         *
         * @param authLimit    nombre max de requêtes auth par fenêtre (défaut : 10)
         * @param authWindowMs durée de fenêtre auth en ms (défaut : 60 000)
         * @param apiLimit     nombre max de requêtes API par fenêtre (défaut : 120)
         * @param apiWindowMs  durée de fenêtre API en ms (défaut : 60 000)
         */
        public record RateLimitProperties(
                        @DefaultValue("10") int authLimit,
                        @DefaultValue("60000") long authWindowMs,
                        @DefaultValue("120") int apiLimit,
                        @DefaultValue("60000") long apiWindowMs) {
        }

        // ── Mail ──────────────────────────────────────────────────────────────

        public record MailProperties(
                        @DefaultValue("no-reply@senpharmaflow.gouv.sn") String from,
                        @DefaultValue("SEN PharmaFlow") String fromName) {
        }
}
