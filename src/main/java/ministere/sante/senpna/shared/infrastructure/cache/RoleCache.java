package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

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

@Component
@Order(0)
public class RoleCache implements ApplicationRunner, RoleCachePort {

    private static final Logger log = LoggerFactory.getLogger(RoleCache.class);

    private final RoleQueryPort roleQueryPort;

    /**
     * Snapshot immutable contenant tous les index du cache.
     */
    private record RoleIndexes(
            Map<UUID, RoleProjection> byId,
            Map<String, RoleProjection> byCode) {
        static RoleIndexes empty() {
            return new RoleIndexes(Map.of(), Map.of());
        }
    }

    /**
     * Référence atomique vers le snapshot courant.
     */
    private final AtomicReference<RoleIndexes> cache = new AtomicReference<>(RoleIndexes.empty());

    public RoleCache(RoleQueryPort roleQueryPort) {
        this.roleQueryPort = roleQueryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        reload();
    }

    /**
     * Recharge entièrement le cache.
     * Les lecteurs voient toujours un snapshot cohérent.
     */
    @Override
    public synchronized void reload() {
        List<RoleProjection> roles = roleQueryPort.findAll();

        Map<UUID, RoleProjection> idIndex = HashMap.newHashMap(roles.size());
        Map<String, RoleProjection> codeIndex = HashMap.newHashMap(roles.size());

        for (RoleProjection role : roles) {
            idIndex.put(role.id(), role);
            codeIndex.put(role.code(), role);
        }

        cache.set(new RoleIndexes(
                Map.copyOf(idIndex),
                Map.copyOf(codeIndex)));

        log.info("[RoleCache] {} rôle(s) chargé(s) en cache", roles.size());
    }

    @Override
    public Optional<RoleProjection> findById(UUID id) {
        return Optional.ofNullable(cache.get().byId().get(id));
    }

    @Override
    public Optional<RoleProjection> findByCode(String code) {
        return Optional.ofNullable(cache.get().byCode().get(code));
    }

    @Override
    public boolean existsById(UUID id) {
        return cache.get().byId().containsKey(id);
    }

    /**
     * Résout le code technique d'un rôle (utilisé pour bâtir les
     * GrantedAuthority de Spring Security).
     *
     * @throws SenPnaException si l'identifiant ne correspond à aucun rôle connu (catégorie NOT_FOUND)
     */
    @Override
    public String getCode(UUID id) {
        return findById(id)
                .map(RoleProjection::code)
                .orElseThrow(() -> new SenPnaException(
                        "Rôle introuvable avec l'identifiant : " + id,
                        "ROLE_NOT_FOUND", ErrorCategory.NOT_FOUND));
    }

    /**
     * Résout un ensemble d'identifiants de rôle en projections.
     * Les identifiants inconnus sont ignorés.
     */
    @Override
    public Set<RoleProjection> findAllById(Set<UUID> ids) {
        Map<UUID, RoleProjection> byId = cache.get().byId();

        Set<RoleProjection> result = HashSet.newHashSet(ids.size());

        for (UUID id : ids) {
            RoleProjection role = byId.get(id);
            if (role != null) {
                result.add(role);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public int size() {
        return cache.get().byId().size();
    }
}
