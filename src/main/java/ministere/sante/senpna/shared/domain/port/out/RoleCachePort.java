package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.RoleProjection;

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

    Set<RoleProjection> findAllById(Set<UUID> ids);
}
