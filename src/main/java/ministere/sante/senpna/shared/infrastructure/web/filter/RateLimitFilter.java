package ministere.sante.senpna.shared.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ministere.sante.senpna.shared.domain.port.out.RateLimitPort;
import ministere.sante.senpna.shared.domain.port.out.RateLimitPort.RateLimitResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

/**
 * Filtre de rate limiting appliqué avant Spring Security.
 *
 * <h3>Correction : /api/auth/resend-otp manquant dans AUTH_PATHS</h3>
 * <p>
 * L'ancienne version ne soumettait {@code /api/auth/resend-otp} qu'au groupe
 * {@code api} (120 req/min). Un attaquant pouvait spammer le renvoi OTP d'une
 * victime à 120 req/min, générant 120 emails/SMS par minute sur des comptes
 * différents. Le cooldown côté domaine ({@code OtpCooldownException}) protège
 * par compte individuel, mais pas contre les attaques en volume multi-comptes.
 * </p>
 *
 * <h3>Groupes et limites</h3>
 * <table>
 * <tr>
 * <th>Groupe</th>
 * <th>Routes</th>
 * <th>Limite</th>
 * <th>Fenêtre</th>
 * </tr>
 * <tr>
 * <td>auth</td>
 * <td>/api/auth/login, /api/auth/register, /api/auth/verify,
 * /api/auth/resend-otp</td>
 * <td>10 req</td>
 * <td>1 min</td>
 * </tr>
 * <tr>
 * <td>webhook</td>
 * <td>/webhooks/**</td>
 * <td>60 req</td>
 * <td>1 min</td>
 * </tr>
 * <tr>
 * <td>api</td>
 * <td>tout le reste en /api/**</td>
 * <td>120 req</td>
 * <td>1 min</td>
 * </tr>
 * </table>
 *
 * <h3>Stratégie de clé</h3>
 * <p>
 * Clé = {@code endpoint_group:ip}. IP extraite depuis {@code X-Forwarded-For}
 * si présent (reverse proxy Nginx / Traefik en prod), sinon {@code remoteAddr}.
 * </p>
 *
 * <h3>Headers retournés</h3>
 * <ul>
 * <li>{@code X-RateLimit-Limit} — limite de la fenêtre</li>
 * <li>{@code X-RateLimit-Remaining} — tokens restants</li>
 * <li>{@code X-RateLimit-Reset} — timestamp UNIX (secondes) de
 * réinitialisation</li>
 * <li>{@code Retry-After} — secondes à attendre (header standard HTTP 429)</li>
 * </ul>
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // ── Limites par groupe ────────────────────────────────────────────────

    /**
     * Endpoints d'authentification — limite stricte anti-bruteforce et anti-spam
     * OTP.
     */
    private static final int AUTH_LIMIT = 10;
    private static final long AUTH_WINDOW_MS = 60_000L;

    /** Webhooks IPN des opérateurs de paiement. */
    private static final int WEBHOOK_LIMIT = 60;
    private static final long WEBHOOK_WINDOW_MS = 60_000L;

    /** API générale authentifiée. */
    private static final int API_LIMIT = 120;
    private static final long API_WINDOW_MS = 60_000L;

    /**
     * Routes d'authentification soumises à la limite stricte (10 req/min par IP).
     *
     * <p>
     * {@code /api/auth/resend-otp} est inclus pour prévenir le spam OTP :
     * sans cette limite, un attaquant peut déclencher 120 envois SMTP/SMS
     * par minute (limite du groupe "api") sur des comptes différents.
     * Le cooldown domaine ({@code OtpCooldownException}) ne protège que
     * par compte individuel.
     * </p>
     */
    private static final Set<String> AUTH_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify",
            "/api/auth/resend-otp",
            "/api/auth/google");

    private final RateLimitPort rateLimitPort;

    public RateLimitFilter(RateLimitPort rateLimitPort) {
        this.rateLimitPort = rateLimitPort;
    }

    // ── Exclusion du Preflight CORS ───────────────────────────────────────

    /**
     * Permet d'ignorer complètement le filtre pour les requêtes HTTP OPTIONS.
     * Indispensable pour laisser Spring Security gérer le mécanisme de Preflight
     * CORS.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = extractIp(request);
        String group = resolveGroup(path);

        if (group == null) {
            // Route non soumise au rate limiting (actuator, swagger…)
            chain.doFilter(request, response);
            return;
        }

        int limit = resolveLimit(group);
        long windowMs = resolveWindow(group);
        String key = group + ":" + ip;

        RateLimitResult result = rateLimitPort.tryConsume(key, limit, windowMs);

        // ── Headers de diagnostic (toujours envoyés) ──────────────────────
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));
        long resetEpochSec = Instant.now().getEpochSecond() + (result.resetAfterMs() / 1000);
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetEpochSec));

        if (!result.allowed()) {
            long retryAfterSec = Math.max(1, result.resetAfterMs() / 1000);
            response.setHeader("Retry-After", String.valueOf(retryAfterSec));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            log.warn("[RateLimit] 429 — group={}, ip={}, path={}", group, ip, path);

            response.getWriter().write(
                    """
                            {
                              "status": 429,
                              "type": "RATE_LIMIT_EXCEEDED",
                              "message": "Trop de requêtes. Veuillez patienter %d seconde(s) avant de réessayer.",
                              "timestamp": "%s",
                              "results": null
                            }
                            """.formatted(retryAfterSec, Instant.now().toString()));
            return;
        }

        chain.doFilter(request, response);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Résout le groupe de rate limiting selon le chemin.
     * Retourne {@code null} pour les routes exclues (actuator, swagger…).
     */
    private String resolveGroup(String path) {
        if (AUTH_PATHS.contains(path))
            return "auth";
        if (path.startsWith("/webhooks/"))
            return "webhook";
        if (path.startsWith("/api/"))
            return "api";
        return null;
    }

    private int resolveLimit(String group) {
        return switch (group) {
            case "auth" -> AUTH_LIMIT;
            case "webhook" -> WEBHOOK_LIMIT;
            default -> API_LIMIT;
        };
    }

    private long resolveWindow(String group) {
        return switch (group) {
            case "auth" -> AUTH_WINDOW_MS;
            case "webhook" -> WEBHOOK_WINDOW_MS;
            default -> API_WINDOW_MS;
        };
    }

    /**
     * Extrait l'IP réelle du client.
     *
     * <p>
     * En production derrière Nginx/Traefik, le vrai IP est dans
     * {@code X-Forwarded-For}. On prend le <em>premier</em> IP de la
     * chaîne (le plus proche du client) en ignorant les proxies intermédiaires.
     * </p>
     */
    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
