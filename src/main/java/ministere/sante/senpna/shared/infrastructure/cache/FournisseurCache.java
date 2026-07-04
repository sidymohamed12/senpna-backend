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

    private volatile Map<UUID, FournisseurProjection> byId = Map.of();

    public FournisseurCache(FournisseurQueryPort fournisseurQueryPort) {
        this.fournisseurQueryPort = fournisseurQueryPort;
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
        List<FournisseurProjection> fournisseurs = fournisseurQueryPort.findAll();

        Map<UUID, FournisseurProjection> idIndex = new HashMap<>();
        for (FournisseurProjection fournisseur : fournisseurs) {
            idIndex.put(fournisseur.id(), fournisseur);
        }

        this.byId = Collections.unmodifiableMap(idIndex);

        log.info("[FournisseurCache] {} fournisseur(s) chargé(s) en cache", fournisseurs.size());
    }

    @Override
    public Optional<FournisseurProjection> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * Résout un ensemble d'identifiants de fournisseur en projections —
     * les identifiants inconnus sont silencieusement ignorés (défensif).
     */
    @Override
    public Set<FournisseurProjection> findAllById(Set<UUID> ids) {
        Set<FournisseurProjection> result = new HashSet<>();
        for (UUID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    public int size() {
        return byId.size();
    }
}
