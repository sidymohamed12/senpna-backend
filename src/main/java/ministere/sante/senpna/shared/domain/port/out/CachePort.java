package ministere.sante.senpna.shared.domain.port.out;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {

    Optional<String> get(String key);

    void put(String key, String value, Duration ttl);

    void evict(String key);

    void evictByPrefix(String prefix);
}
