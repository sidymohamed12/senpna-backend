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
                MailProperties mail,
                CacheProperties cache) {

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

        // ── Cache Redis (clé/valeur, hors régions/fournisseurs/formes/familles) ──
        //
        // Contrairement aux référentiels quasi-statiques (régions, fournisseurs,
        // formes, familles — chargés intégralement en mémoire au démarrage, cf.
        // *Cache dans shared/organisation/medicament), les agrégats ci-dessous
        // changent trop souvent ou sont trop volumineux pour être préchargés :
        // ils sont mis en cache à la clé (Redis, cache-aside) uniquement lors de
        // leur consultation, avec un TTL court et une éviction explicite à
        // chaque écriture.

        /**
         * TTL du cache applicatif clé/valeur (Redis) par agrégat/feature.
         *
         * @param medicamentTtl TTL du cache {@code medicament:id:<id>}
         * @param lotTtl        TTL du cache {@code lot:id:<id>}
         * @param stockTtl      TTL du cache {@code stock:id:<id>} /
         *                      {@code stock:entrepot:<id>:lot:<id>}
         * @param mouvementTtl  TTL du cache {@code mouvement:id:<id>} — long,
         *                      le journal des mouvements est immuable une fois
         *                      écrit (append-only)
         * @param userTtl       TTL du cache {@code user:id:<id>}
         * @param catalogueTtl  TTL du cache des pages de catalogue assemblées
         *                      ({@code catalogue:national:*},
         *                      {@code catalogue:regional:*},
         *                      {@code catalogue:inter-pra:*})
         * @param projetTtl     TTL du cache {@code projet:id:<id>}
         * @param actualiteTtl  TTL du cache {@code actualite:id:<id>}
         */
        public record CacheProperties(
                        @DefaultValue("PT10M") Duration medicamentTtl,
                        @DefaultValue("PT5M") Duration lotTtl,
                        @DefaultValue("PT2M") Duration stockTtl,
                        @DefaultValue("PT6H") Duration mouvementTtl,
                        @DefaultValue("PT5M") Duration userTtl,
                        @DefaultValue("PT1M") Duration catalogueTtl,
                        @DefaultValue("PT5M") Duration projetTtl,
                        @DefaultValue("PT5M") Duration actualiteTtl) {
        }
}
