package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.infrastructure.cache.RoleCache;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RoleSummary;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Résout un ensemble d'identifiants de rôle en résumés affichables
 * (id, code, nom) pour l'administration des utilisateurs, via
 * {@link RoleCache} — évite un aller-retour SQL par utilisateur affiché
 * (N+1), suivant la même stratégie que {@code UserRoleResolver}
 * (utilisé lors du login).
 */
@Component
public class UserRoleSummaryResolver {

    private final RoleCache roleCache;

    public UserRoleSummaryResolver(RoleCache roleCache) {
        this.roleCache = roleCache;
    }

    public Set<RoleSummary> resoudre(Set<UUID> roleIds) {
        return roleCache.findAllById(roleIds).stream()
                .map(this::toSummary)
                .collect(Collectors.toUnmodifiableSet());
    }

    private RoleSummary toSummary(RoleProjection projection) {
        return new RoleSummary(projection.id(), projection.code(), projection.nom());
    }
}
