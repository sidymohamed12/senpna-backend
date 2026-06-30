package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.infrastructure.exception.NotFoundException;

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

@Component
@Order(0)
public class RoleCache implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleCache.class);

    private final RoleQueryPort roleQueryPort;

    private volatile Map<UUID, RoleProjection> byId = Map.of();
    private volatile Map<String, RoleProjection> byCode = Map.of();

    public RoleCache(RoleQueryPort roleQueryPort) {
        this.roleQueryPort = roleQueryPort;
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
    public synchronized void reload() {
        List<RoleProjection> roles = roleQueryPort.findAll();

        Map<UUID, RoleProjection> idIndex = new HashMap<>();
        Map<String, RoleProjection> codeIndex = new HashMap<>();
        for (RoleProjection role : roles) {
            idIndex.put(role.id(), role);
            codeIndex.put(role.code(), role);
        }

        this.byId = Collections.unmodifiableMap(idIndex);
        this.byCode = Collections.unmodifiableMap(codeIndex);

        log.info("[RoleCache] {} rôle(s) chargé(s) en cache", roles.size());
    }

    public Optional<RoleProjection> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<RoleProjection> findByCode(String code) {
        return Optional.ofNullable(byCode.get(code));
    }

    public boolean existsById(UUID id) {
        return byId.containsKey(id);
    }

    /**
     * Résout le code technique d'un rôle (utilisé pour bâtir les
     * {@code GrantedAuthority} Spring Security).
     *
     * @throws NotFoundException si l'identifiant ne correspond à aucun rôle connu
     */
    public String getCode(UUID id) {
        return findById(id)
                .map(RoleProjection::code)
                .orElseThrow(() -> new NotFoundException(
                        "Rôle introuvable avec l'identifiant : " + id, "ROLE_NOT_FOUND"));
    }

    /**
     * Résout un ensemble d'identifiants de rôle en projections — les
     * identifiants inconnus sont silencieusement ignorés (défensif : un
     * rôle a pu être retiré du référentiel sans que l'utilisateur ait
     * encore été mis à jour).
     */
    public Set<RoleProjection> findAllById(Set<UUID> ids) {
        Set<RoleProjection> result = new HashSet<>();
        for (UUID id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    public int size() {
        return byId.size();
    }
}
