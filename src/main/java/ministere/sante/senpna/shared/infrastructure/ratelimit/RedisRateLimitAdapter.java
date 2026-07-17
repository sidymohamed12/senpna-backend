package ministere.sante.senpna.shared.infrastructure.ratelimit;

import ministere.sante.senpna.shared.domain.port.out.RateLimitPort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptateur rate limiting Redis — actif sur les profils {@code dev} et
 * {@code prod}.
 *
 * <h3>Algorithme : Sliding Window Counter via Lua</h3>
 * <p>
 * Un script Lua atomique incrémente un compteur Redis avec expiration.
 * L'atomicité garantit l'absence de race conditions en environnement
 * multi-instance.
 * </p>
 *
 * <h3>Pourquoi Lua ?</h3>
 * <p>
 * Redis exécute les scripts Lua de manière atomique (single-threaded).
 * Sans script, INCR + EXPIRE en deux appels séparés crée une race condition
 * entre les instances de l'application (microservices / réplicas Spring Boot).
 * Avec Lua, les deux opérations sont une seule transaction Redis.
 * </p>
 *
 * <h3>Structure de la clé Redis</h3>
 * 
 * <pre>
 * ratelimit:{group}:{ip}  →  compteur (String, auto-expire après windowMs)
 * </pre>
 *
 * <h3>Complexité</h3>
 * <p>
 * O(1) par requête. Mémoire : une clé String par (group, IP) active.
 * TTL = windowMs → les clés inactives expirent automatiquement.
 * </p>
 */
@Component
@Profile({ "dev", "prod" })
public class RedisRateLimitAdapter implements RateLimitPort {

    /**
     * Script Lua : INCR + PEXPIRE atomique.
     *
     * <p>
     * ARGV[1] = windowMs (TTL en millisecondes)
     * ARGV[2] = limit (nombre max de requêtes)
     * Retourne : {count, ttlMs} où count est le compteur après incrément.
     * </p>
     */
    private static final String SLIDING_WINDOW_LUA = """
            local key = KEYS[1]
            local windowMs = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])

            local count = redis.call('INCR', key)
            if count == 1 then
                redis.call('PEXPIRE', key, windowMs)
            end

            local ttlMs = redis.call('PTTL', key)
            return {count, ttlMs}
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List<Long>> rateLimitScript;

    @SuppressWarnings("unchecked")
    public RedisRateLimitAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = (DefaultRedisScript<List<Long>>) (DefaultRedisScript<?>) new DefaultRedisScript<>(
                SLIDING_WINDOW_LUA, List.class);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * En cas d'erreur Redis (connexion perdue), la méthode fail-open :
     * retourne {@code allowed=true} avec remaining=1. Préférable à bloquer
     * toutes les requêtes légitimes lors d'une panne Redis temporaire.
     * </p>
     */
    @Override
    public RateLimitResult tryConsume(String key, int limit, long windowMs) {
        try {
            List<Long> result = redisTemplate.execute(
                    rateLimitScript,
                    List.of("ratelimit:" + key),
                    String.valueOf(windowMs),
                    String.valueOf(limit));

            if (result == null || result.size() < 2) {
                return RateLimitResult.allow(limit - 1L, windowMs);
            }

            long count = result.get(0);
            long ttlMs = Math.max(0, result.get(1));
            long remaining = Math.max(0, limit - count);

            if (count > limit) {
                return RateLimitResult.deny(ttlMs);
            }
            return RateLimitResult.allow(remaining, ttlMs);

        } catch (Exception e) {
            // Fail-open : Redis indisponible → autoriser la requête
            return RateLimitResult.allow(1L, windowMs);
        }
    }
}
