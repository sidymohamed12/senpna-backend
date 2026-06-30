package ministere.sante.senpna.shared.infrastructure.ratelimit;

import ministere.sante.senpna.shared.domain.port.out.RateLimitPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("test")
public class InMemoryRateLimitAdapter implements RateLimitPort {

    /**
     * Entrée de rate limit : compteur + début de fenêtre.
     */
    private static class WindowEntry {
        final AtomicLong count = new AtomicLong(0);
        volatile long windowStartMs = System.currentTimeMillis();
    }

    private final Map<String, WindowEntry> windows = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult tryConsume(String key, int limit, long windowMs) {
        WindowEntry entry = windows.computeIfAbsent(key, k -> new WindowEntry());

        long now = System.currentTimeMillis();
        long elapsed = now - entry.windowStartMs;

        // Réinitialise la fenêtre si expirée
        if (elapsed >= windowMs) {
            entry.count.set(0);
            entry.windowStartMs = now;
        }

        long currentCount = entry.count.incrementAndGet();
        long resetAfterMs = Math.max(0, windowMs - (now - entry.windowStartMs));
        long remaining = Math.max(0, limit - currentCount);

        if (currentCount > limit) {
            return RateLimitResult.deny(resetAfterMs);
        }
        return RateLimitResult.allow(remaining, resetAfterMs);
    }

    /**
     * Réinitialise tous les compteurs.
     */
    public void reset() {
        windows.clear();
    }
}
