package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Profile({ "dev", "prod" })
public class RedisCacheAdapter implements CachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheAdapter.class);

    /**
     * Nombre de clés inspectées par itération SCAN. Une valeur basse réduit
     * le temps de blocage par appel (proche de zéro), au prix d'un nombre
     * plus élevé d'allers-retours. 200 est un compromis raisonnable pour
     * des groupes de cache de quelques centaines à quelques milliers de clés.
     */
    private static final int SCAN_BATCH_SIZE = 200;

    private final StringRedisTemplate redisTemplate;

    public RedisCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.warn("[Cache] Erreur lors de la lecture Redis, clé={} : {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[Cache] Erreur lors de l'écriture Redis, clé={} : {}", key, e.getMessage());
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[Cache] Erreur lors de la suppression Redis, clé={} : {}", key, e.getMessage());
        }
    }

    @Override
    public void evictByPrefix(String prefix) {
        try {
            List<String> keysToDelete = scanKeys(prefix + "*");

            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                log.debug("[Cache] {} clé(s) supprimée(s) avec le préfixe '{}'",
                        keysToDelete.size(), prefix);
            }
        } catch (Exception e) {
            log.warn("[Cache] Erreur lors de l'invalidation Redis, préfixe={} : {}", prefix, e.getMessage());
        }
    }

    // ── Helpers privés ────────────────────────────────────────────────────

    /**
     * Parcourt le keyspace via {@code SCAN} et collecte toutes les clés
     * correspondant au pattern donné.
     * s
     * 
     * @param pattern pattern Redis (ex: {@code "medicament:catalogue:*"})
     * @return liste des clés correspondantes — jamais null
     */
    private List<String> scanKeys(String pattern) {
        List<String> result = new ArrayList<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(SCAN_BATCH_SIZE)
                .build();

        // executeWithStickyConnection garantit que le curseur SCAN reste
        // valide sur la même connexion physique pendant toute l'itération
        redisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return null;
        }, true);

        return result;
    }
}