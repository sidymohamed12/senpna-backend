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

    private volatile Map<UUID, FormeProjection> byId = Map.of();

    public FormeCache(FormeQueryPort formeQueryPort) {
        this.formeQueryPort = formeQueryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        reload();
    }

    /**
     * Recharge intégralement le cache depuis la source de vérité.
     * Thread-safe : la map est remplacée atomiquement (publication via
     * référence volatile), aucun verrou nécessaire en lecture.
     */
    @Override
    public synchronized void reload() {
        List<FormeProjection> formes = formeQueryPort.findAll();

        Map<UUID, FormeProjection> idIndex = new HashMap<>();
        for (FormeProjection forme : formes) {
            idIndex.put(forme.id(), forme);
        }

        this.byId = Collections.unmodifiableMap(idIndex);

        log.info("[FormeCache] {} forme(s) chargée(s) en cache", formes.size());
    }

    @Override
    public Optional<FormeProjection> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return byId.containsKey(id);
    }

    /**
     * Résout un ensemble d'identifiants de forme en projections — les
     * identifiants inconnus sont silencieusement ignorés (défensif).
     */
    @Override
    public Set<FormeProjection> findAllById(Set<UUID> ids) {
        Set<FormeProjection> result = new HashSet<>();
        for (UUID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    public int size() {
        return byId.size();
    }
}
