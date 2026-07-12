package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.RegionCachePort;
import ministere.sante.senpna.shared.domain.port.out.RegionQueryPort;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cache en mémoire des régions — chargé au démarrage puis rechargé à
 * chaque création/modification (cf. {@link RegionCachePort#reload()}).
 * Suit exactement la même stratégie que {@link RoleCache}.
 */
@Component
@Order(0)
public class RegionCache implements ApplicationRunner, RegionCachePort {

    private static final Logger log = LoggerFactory.getLogger(RegionCache.class);

    private final RegionQueryPort regionQueryPort;

    /**
     * Snapshot immutable contenant tous les index du cache.
     * Les lecteurs accèdent toujours à un snapshot cohérent.
     */
    private record RegionIndexes(
            Map<UUID, RegionProjection> byId,
            Map<String, RegionProjection> byCode) {
        static RegionIndexes empty() {
            return new RegionIndexes(Map.of(), Map.of());
        }
    }

    /**
     * Référence atomique vers le snapshot courant.
     */
    private final AtomicReference<RegionIndexes> cache = new AtomicReference<>(RegionIndexes.empty());

    public RegionCache(RegionQueryPort regionQueryPort) {
        this.regionQueryPort = regionQueryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        reload();
    }

    /**
     * Recharge entièrement le cache.
     * La publication est atomique : tous les lecteurs voient soit
     * l'ancien snapshot, soit le nouveau, jamais un état intermédiaire.
     */
    @Override
    public synchronized void reload() {
        List<RegionProjection> regions = regionQueryPort.findAll();

        Map<UUID, RegionProjection> idIndex = HashMap.newHashMap(regions.size());
        Map<String, RegionProjection> codeIndex = HashMap.newHashMap(regions.size());

        for (RegionProjection region : regions) {
            idIndex.put(region.id(), region);
            codeIndex.put(region.code(), region);
        }

        cache.set(new RegionIndexes(
                Map.copyOf(idIndex),
                Map.copyOf(codeIndex)));

        log.info("[RegionCache] {} région(s) chargée(s) en cache", regions.size());
    }

    @Override
    public Optional<RegionProjection> findById(UUID id) {
        return Optional.ofNullable(cache.get().byId().get(id));
    }

    public Optional<RegionProjection> findByCode(String code) {
        return Optional.ofNullable(cache.get().byCode().get(code));
    }

    @Override
    public boolean existsById(UUID id) {
        return cache.get().byId().containsKey(id);
    }

    /**
     * Résout un ensemble d'identifiants de région en projections.
     * Les identifiants inconnus sont ignorés.
     */
    @Override
    public Set<RegionProjection> findAllById(Set<UUID> ids) {
        Map<UUID, RegionProjection> byId = cache.get().byId();

        Set<RegionProjection> result = HashSet.newHashSet(ids.size());

        for (UUID id : ids) {
            RegionProjection region = byId.get(id);
            if (region != null) {
                result.add(region);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public int size() {
        return cache.get().byId().size();
    }
}
