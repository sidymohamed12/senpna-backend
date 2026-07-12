package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.FormeCachePort;
import ministere.sante.senpna.shared.domain.port.out.FormeQueryPort;
import ministere.sante.senpna.shared.domain.projection.FormeProjection;

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
 * Cache en mémoire des formes pharmaceutiques — chargé au démarrage puis
 * rechargé à chaque création/modification/archivage (cf.
 * {@link FormeCachePort#reload()}). Référentiel quasi-statique (à
 * l'inverse du référentiel médicament, potentiellement volumineux, mis en
 * cache Redis à la demande — cf. {@code CachingMedicamentRepositoryAdapter}) :
 * suit exactement la même stratégie que {@link RegionCache} /
 * {@link FournisseurCache}.
 */
@Component
@Order(0)
public class FormeCache implements ApplicationRunner, FormeCachePort {

    private static final Logger log = LoggerFactory.getLogger(FormeCache.class);

    private final FormeQueryPort formeQueryPort;

    /**
     * Snapshot immutable du cache.
     */
    private final AtomicReference<Map<UUID, FormeProjection>> cache = new AtomicReference<>(Map.of());

    public FormeCache(FormeQueryPort formeQueryPort) {
        this.formeQueryPort = formeQueryPort;
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
        List<FormeProjection> formes = formeQueryPort.findAll();

        Map<UUID, FormeProjection> idIndex = HashMap.newHashMap(formes.size());

        for (FormeProjection forme : formes) {
            idIndex.put(forme.id(), forme);
        }

        cache.set(Map.copyOf(idIndex));

        log.info("[FormeCache] {} forme(s) chargée(s) en cache", formes.size());
    }

    @Override
    public Optional<FormeProjection> findById(UUID id) {
        return Optional.ofNullable(cache.get().get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return cache.get().containsKey(id);
    }

    /**
     * Résout un ensemble d'identifiants de forme en projections.
     * Les identifiants inconnus sont ignorés.
     */
    @Override
    public Set<FormeProjection> findAllById(Set<UUID> ids) {
        Map<UUID, FormeProjection> byId = cache.get();

        Set<FormeProjection> result = HashSet.newHashSet(ids.size());

        for (UUID id : ids) {
            FormeProjection forme = byId.get(id);
            if (forme != null) {
                result.add(forme);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public int size() {
        return cache.get().size();
    }
}
