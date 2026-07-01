package ministere.sante.senpna.auth.infrastructure.security;

import io.jsonwebtoken.JwtException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;
import ministere.sante.senpna.config.JwtService;
import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/**
 * Adapter JWT + révocation Redis du {@link TokenPort}.
 *
 * <h3>Liste noire (révocation)</h3>
 * <p>
 * Un JWT est <em>stateless par nature</em> : une fois signé, il est valide
 * jusqu'à son expiration, même après déconnexion ou changement de mot de
 * passe. La liste noire Redis corrige cela :
 * </p>
 * <ol>
 * <li>À la révocation, on calcule le <strong>SHA-256 du token</strong> et on
 * le stocke en Redis avec un TTL égal à la durée de vie résiduelle du
 * token.</li>
 * <li>À chaque requête, {@code JwtAuthenticationFilter} appelle
 * {@link #estInvalide(String)} avant d'authentifier l'utilisateur.</li>
 * <li>Quand le token expire naturellement, Redis supprime l'entrée
 * automatiquement — aucune purge manuelle nécessaire.</li>
 * </ol>
 *
 * <h3>Pourquoi SHA-256 et non le token brut ?</h3>
 * <p>
 * Stocker le JWT complet en Redis exposerait le secret en cas de dump Redis.
 * Un hash à sens unique est suffisant pour l'identification et ne permet pas
 * de reconstituer le token.
 * </p>
 *
 * <h3>Gestion des tokens déjà expirés</h3>
 * <p>
 * Si {@code invalider()} est appelé sur un token déjà expiré, on calcule
 * une TTL résiduelle ≤ 0. Dans ce cas, on ignore l'insertion (inutile :
 * le token est déjà invalide par expiration). On logue un warning pour
 * détecter d'éventuels appels suspects.
 * </p>
 */
@Component
public class JwtTokenAdapter implements TokenPort {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenAdapter.class);
    private static final String REVOCATION_PREFIX = "auth:revoked:";

    private final JwtService jwtService;
    private final CachePort cachePort;

    public JwtTokenAdapter(JwtService jwtService, CachePort cachePort) {
        this.jwtService = jwtService;
        this.cachePort = cachePort;
    }

    // ── Génération ────────────────────────────────────────────────────────

    @Override
    public String genererAccess(User user, Set<String> roleCodes) {
        return jwtService.generateAccessToken(
                user.getEmail().value(),
                Map.of("roles", roleCodes));
    }

    @Override
    public String genererRefresh(User user) {
        return jwtService.generateRefreshToken(user.getEmail().value());
    }

    // ── Révocation ────────────────────────────────────────────────────────

    @Override
    public void invalider(String token) {
        try {
            Date expiration = jwtService.extractExpiration(token);
            long ttlMs = expiration.getTime() - Instant.now().toEpochMilli();

            if (ttlMs <= 0) {
                log.debug("[TokenRevocation] Token déjà expiré, révocation inutile.");
                return;
            }

            String fingerprint = sha256(token);
            cachePort.put(REVOCATION_PREFIX + fingerprint, "1", Duration.ofMillis(ttlMs));
            log.info("[TokenRevocation] Token révoqué (TTL résiduelle={} ms)", ttlMs);

        } catch (JwtException e) {
            log.warn("[TokenRevocation] Tentative de révocation d'un token malformé : {}", e.getMessage());
        }
    }

    @Override
    public boolean estInvalide(String token) {
        try {
            String fingerprint = sha256(token);
            return cachePort.get(REVOCATION_PREFIX + fingerprint).isPresent();
        } catch (Exception e) {
            // En cas d'erreur Redis, on échoue de manière permissive (fail-open) pour
            // ne pas bloquer tous les utilisateurs. À ajuster selon le niveau de sécurité
            // requis.
            log.error("[TokenRevocation] Impossible de vérifier la révocation, fail-open : {}", e.getMessage());
            return false;
        }
    }

    // ── Extraction ────────────────────────────────────────────────────────

    @Override
    public String extraireEmail(String token) {
        return jwtService.extractUsername(token);
    }

    @Override
    public boolean estRefreshToken(String token) {
        return jwtService.isRefreshToken(token);
    }

    @Override
    public boolean estExpire(String token) {
        try {
            return jwtService.extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti dans tout JDK — ne peut pas se produire
            throw new IllegalStateException("SHA-256 non disponible", e);
        }
    }
}
