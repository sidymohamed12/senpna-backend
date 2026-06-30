package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.shared.infrastructure.cache.RoleCache;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Résout un ensemble d'identifiants de rôle en codes techniques
 * (ex: {@code ADMIN_PNA}), via {@link RoleCache} — mutualisé entre
 * {@code LoginUseCaseImpl}, {@code RefreshTokenUseCaseImpl} et
 * {@code MeUseCaseImpl}.
 */
@Component
public class UserRoleResolver {

    private final RoleCache roleCache;

    public UserRoleResolver(RoleCache roleCache) {
        this.roleCache = roleCache;
    }

    public Set<String> resoudreCodes(Set<UUID> roleIds) {
        return roleCache.findAllById(roleIds).stream()
                .map(role -> role.code())
                .collect(Collectors.toUnmodifiableSet());
    }
}
