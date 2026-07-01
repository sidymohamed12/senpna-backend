package ministere.sante.senpna.auth.infrastructure.cache;

import ministere.sante.senpna.auth.domain.exception.ResetTokenInvalideException;
import ministere.sante.senpna.auth.domain.port.out.ResetTokenPort;
import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Implémentation Redis du {@link ResetTokenPort}.
 *
 * <h3>Format de stockage</h3>
 * <p>
 * Clé : {@code auth:reset:<uuid>}<br>
 * Valeur : {@code <userId>|<email>} (séparateur {@code |}, car l'email ne
 * peut pas le contenir — RFC 5321).<br>
 * TTL : 5 minutes (non configurable pour l'instant, assez court pour
 * limiter la fenêtre d'attaque, assez long pour une UX raisonnable).
 * </p>
 *
 * <h3>Usage unique</h3>
 * <p>
 * {@link #validerEtExtraireUserId} effectue un get puis un evict
 * atomique du point de vue métier (Redis ne supporte pas de transaction
 * get+delete atomique sans Lua ; la fenêtre de compétition est acceptable
 * car les jetons sont à court TTL et liés à un utilisateur identifié).
 * </p>
 */
@Component
public class RedisResetTokenAdapter implements ResetTokenPort {

    private static final Logger log = LoggerFactory.getLogger(RedisResetTokenAdapter.class);

    private static final String PREFIX = "auth:reset:";
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String SEPARATOR = "|";

    private final CachePort cachePort;

    public RedisResetTokenAdapter(CachePort cachePort) {
        this.cachePort = cachePort;
    }

    @Override
    public String genererResetToken(UUID userId, String email) {
        String token = UUID.randomUUID().toString();
        String value = userId.toString() + SEPARATOR + email;
        cachePort.put(PREFIX + token, value, TTL);
        log.debug("[ResetToken] Jeton généré pour userId={}", userId);
        return token;
    }

    @Override
    public UUID validerEtExtraireUserId(String resetToken) {
        String value = cachePort.get(PREFIX + resetToken)
                .orElseThrow(ResetTokenInvalideException::new);

        // Consommation immédiate — usage unique
        cachePort.evict(PREFIX + resetToken);

        String[] parts = value.split("\\" + SEPARATOR, 2);
        if (parts.length != 2) {
            log.error("[ResetToken] Format invalide en Redis pour le jeton {}", resetToken);
            throw new ResetTokenInvalideException();
        }

        return UUID.fromString(parts[0]);
    }

    @Override
    public void invaliderResetToken(String resetToken) {
        cachePort.evict(PREFIX + resetToken);
    }
}
