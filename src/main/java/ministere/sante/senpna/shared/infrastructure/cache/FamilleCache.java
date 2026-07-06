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

    private volatile Map<UUID, FamilleProjection> byId = Map.of();

    public FamilleCache(FamilleQueryPort familleQueryPort) {
        this.familleQueryPort = familleQueryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        reload();
    }

    @Override
    public synchronized void reload() {
        List<FamilleProjection> familles = familleQueryPort.findAll();

        Map<UUID, FamilleProjection> idIndex = new HashMap<>();
        for (FamilleProjection famille : familles) {
            idIndex.put(famille.id(), famille);
        }

        this.byId = Collections.unmodifiableMap(idIndex);

        log.info("[FamilleCache] {} famille(s) chargée(s) en cache", familles.size());
    }

    @Override
    public Optional<FamilleProjection> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return byId.containsKey(id);
    }

    @Override
    public Set<FamilleProjection> findAllById(Set<UUID> ids) {
        Set<FamilleProjection> result = new HashSet<>();
        for (UUID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    public int size() {
        return byId.size();
    }
}
