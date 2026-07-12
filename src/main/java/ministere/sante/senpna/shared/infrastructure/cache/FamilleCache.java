package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.FamilleCachePort;
import ministere.sante.senpna.shared.domain.port.out.FamilleQueryPort;
import ministere.sante.senpna.shared.domain.projection.FamilleProjection;

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
 * Cache en mémoire des familles thérapeutiques — chargé au démarrage puis
 * rechargé à chaque création/modification/archivage (cf.
 * {@link FamilleCachePort#reload()}). Référentiel quasi-statique : suit
 * exactement la même stratégie que {@link RegionCache} /
 * {@link FournisseurCache} / {@link FormeCache}.
 */
@Component
@Order(0)
public class FamilleCache implements ApplicationRunner, FamilleCachePort {

    private static final Logger log = LoggerFactory.getLogger(FamilleCache.class);

    private final FamilleQueryPort familleQueryPort;

    /**
     * Snapshot immutable du cache.
     */
    private final AtomicReference<Map<UUID, FamilleProjection>> cache = new AtomicReference<>(Map.of());

    public FamilleCache(FamilleQueryPort familleQueryPort) {
        this.familleQueryPort = familleQueryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        reload();
    }

    /**
     * Recharge entièrement le cache.
     * Les lecteurs voient toujours soit l'ancien snapshot,
     * soit le nouveau, jamais un état intermédiaire.
     */
    @Override
    public synchronized void reload() {
        List<FamilleProjection> familles = familleQueryPort.findAll();

        Map<UUID, FamilleProjection> idIndex = HashMap.newHashMap(familles.size());

        for (FamilleProjection famille : familles) {
            idIndex.put(famille.id(), famille);
        }

        cache.set(Map.copyOf(idIndex));

        log.info("[FamilleCache] {} famille(s) chargée(s) en cache", familles.size());
    }

    @Override
    public Optional<FamilleProjection> findById(UUID id) {
        return Optional.ofNullable(cache.get().get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return cache.get().containsKey(id);
    }

    @Override
    public Set<FamilleProjection> findAllById(Set<UUID> ids) {
        Map<UUID, FamilleProjection> byId = cache.get();

        Set<FamilleProjection> result = HashSet.newHashSet(ids.size());

        for (UUID id : ids) {
            FamilleProjection famille = byId.get(id);
            if (famille != null) {
                result.add(famille);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public int size() {
        return cache.get().size();
    }
}
