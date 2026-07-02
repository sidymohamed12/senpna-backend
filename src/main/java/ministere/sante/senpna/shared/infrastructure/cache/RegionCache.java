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

    private volatile Map<UUID, RegionProjection> byId = Map.of();
    private volatile Map<String, RegionProjection> byCode = Map.of();

    public RegionCache(RegionQueryPort regionQueryPort) {
        this.regionQueryPort = regionQueryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        reload();
    }

    /**
     * Recharge intégralement le cache depuis la source de vérité.
     * Thread-safe : les maps sont remplacées atomiquement (publication
     * via référence volatile), aucun verrou nécessaire en lecture.
     */
    @Override
    public synchronized void reload() {
        List<RegionProjection> regions = regionQueryPort.findAll();

        Map<UUID, RegionProjection> idIndex = new HashMap<>();
        Map<String, RegionProjection> codeIndex = new HashMap<>();
        for (RegionProjection region : regions) {
            idIndex.put(region.id(), region);
            codeIndex.put(region.code(), region);
        }

        this.byId = Collections.unmodifiableMap(idIndex);
        this.byCode = Collections.unmodifiableMap(codeIndex);

        log.info("[RegionCache] {} région(s) chargée(s) en cache", regions.size());
    }

    @Override
    public Optional<RegionProjection> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<RegionProjection> findByCode(String code) {
        return Optional.ofNullable(byCode.get(code));
    }

    @Override
    public boolean existsById(UUID id) {
        return byId.containsKey(id);
    }

    /**
     * Résout un ensemble d'identifiants de région en projections — les
     * identifiants inconnus sont silencieusement ignorés (défensif).
     */
    @Override
    public Set<RegionProjection> findAllById(Set<UUID> ids) {
        Set<RegionProjection> result = new HashSet<>();
        for (UUID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    public int size() {
        return byId.size();
    }
}
