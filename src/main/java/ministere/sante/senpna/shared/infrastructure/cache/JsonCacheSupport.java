package ministere.sante.senpna.shared.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.shared.domain.port.out.CachePort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Sérialisation JSON au-dessus de {@link CachePort} — {@link CachePort}
 * ne connaît que des chaînes (Redis en dev/prod, in-memory en test) ;
 * cette classe évite de dupliquer la sérialisation/désérialisation
 * Jackson (et sa gestion d'erreurs) dans chaque adaptateur de cache
 * applicatif ({@code Caching*RepositoryAdapter}).
 *
 * <p>
 * Stratégie d'erreur : identique à {@code RedisCacheAdapter} — le cache
 * est un accélérateur, jamais une source de vérité. Toute erreur de
 * (dé)sérialisation dégrade silencieusement en cache miss / no-op plutôt
 * que de faire échouer l'appelant : mieux vaut un aller-retour base de
 * données superflu qu'une requête en erreur à cause du cache.
 * </p>
 */
@Component
public class JsonCacheSupport {

    private static final Logger log = LoggerFactory.getLogger(JsonCacheSupport.class);

    private final CachePort cachePort;
    private final ObjectMapper objectMapper;

    public JsonCacheSupport(CachePort cachePort, ObjectMapper objectMapper) {
        this.cachePort = cachePort;
        this.objectMapper = objectMapper;
    }

    /**
     * Lit et désérialise la valeur associée à {@code key}, si présente et
     * valide.
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        return cachePort.get(key).flatMap(json -> deserialiser(key, json, type));
    }

    /**
     * Sérialise {@code value} et l'écrit sous {@code key} avec le TTL
     * donné. No-op silencieux si la sérialisation échoue.
     */
    public void put(String key, Object value, Duration ttl) {
        try {
            cachePort.put(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.warn("[JsonCache] Erreur de sérialisation, clé={} : {}", key, e.getMessage());
        }
    }

    public void evict(String key) {
        cachePort.evict(key);
    }

    public void evictByPrefix(String prefix) {
        cachePort.evictByPrefix(prefix);
    }

    private <T> Optional<T> deserialiser(String key, String json, Class<T> type) {
        try {
            return Optional.ofNullable(objectMapper.readValue(json, type));
        } catch (Exception e) {
            log.warn("[JsonCache] Erreur de désérialisation, clé={} : {} — purge de l'entrée invalide", key,
                    e.getMessage());
            cachePort.evict(key);
            return Optional.empty();
        }
    }
}
