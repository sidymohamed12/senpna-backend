package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.RoleProjection;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Résolution de rôles à partir d'un cache en mémoire — distinct de
 * {@link RoleQueryPort}, qui interroge la source de vérité (base de
 * données). Permet aux use cases de résoudre des codes de rôle à haute
 * fréquence (login, JWT, listing d'utilisateurs) sans dépendre
 * directement d'une implémentation d'infrastructure concrète.
 */
public interface RoleCachePort {
    void reload();

    Optional<RoleProjection> findById(UUID id);

    Optional<RoleProjection> findByCode(String code);

    boolean existsById(UUID id);

    String getCode(UUID id);

    Set<RoleProjection> findAllById(Set<UUID> ids);
}
