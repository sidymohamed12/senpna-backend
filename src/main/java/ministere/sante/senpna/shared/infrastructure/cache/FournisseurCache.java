package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;
import ministere.sante.senpna.shared.domain.port.out.FournisseurQueryPort;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;

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
 * Cache en mémoire des fournisseurs — chargé au démarrage puis rechargé à
 * chaque création/modification (cf. {@link FournisseurCachePort#reload()}).
 * Suit exactement la même stratégie que {@link RoleCache} /
 * {@link RegionCache}.
 */
@Component
@Order(0)
public class FournisseurCache implements ApplicationRunner, FournisseurCachePort {

    private static final Logger log = LoggerFactory.getLogger(FournisseurCache.class);

    private final FournisseurQueryPort fournisseurQueryPort;

    /**
     * Snapshot immutable du cache.
     */
    private final AtomicReference<Map<UUID, FournisseurProjection>> cache = new AtomicReference<>(Map.of());

    public FournisseurCache(FournisseurQueryPort fournisseurQueryPort) {
        this.fournisseurQueryPort = fournisseurQueryPort;
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
        List<FournisseurProjection> fournisseurs = fournisseurQueryPort.findAll();

        Map<UUID, FournisseurProjection> idIndex = HashMap.newHashMap(fournisseurs.size());

        for (FournisseurProjection fournisseur : fournisseurs) {
            idIndex.put(fournisseur.id(), fournisseur);
        }

        cache.set(Map.copyOf(idIndex));

        log.info("[FournisseurCache] {} fournisseur(s) chargé(s) en cache", fournisseurs.size());
    }

    @Override
    public Optional<FournisseurProjection> findById(UUID id) {
        return Optional.ofNullable(cache.get().get(id));
    }

    /**
     * Résout un ensemble d'identifiants de fournisseur en projections.
     * Les identifiants inconnus sont ignorés.
     */
    @Override
    public Set<FournisseurProjection> findAllById(Set<UUID> ids) {
        Map<UUID, FournisseurProjection> byId = cache.get();

        Set<FournisseurProjection> result = HashSet.newHashSet(ids.size());

        for (UUID id : ids) {
            FournisseurProjection fournisseur = byId.get(id);
            if (fournisseur != null) {
                result.add(fournisseur);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public int size() {
        return cache.get().size();
    }
}
