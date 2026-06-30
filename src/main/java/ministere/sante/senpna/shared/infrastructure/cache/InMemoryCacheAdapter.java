package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.CachePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class InMemoryCacheAdapter implements CachePort {

    /**
     * Entrée de cache : valeur + instant d'expiration.
     */
    private record CacheEntry(String value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    @Override
    public Optional<String> get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        store.put(key, new CacheEntry(value, Instant.now().plus(ttl)));
    }

    @Override
    public void evict(String key) {
        store.remove(key);
    }

    @Override
    public void evictByPrefix(String prefix) {
        store.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public void clear() {
        store.clear();
    }

    /**
     * Nombre d'entrées non expirées dans le cache — utile pour les assertions de
     * test.
     */
    public long activeSize() {
        return store.values().stream().filter(e -> !e.isExpired()).count();
    }
}
