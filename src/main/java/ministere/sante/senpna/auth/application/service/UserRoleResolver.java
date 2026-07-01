package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Résout un ensemble d'identifiants de rôle en codes techniques
 * (ex: {@code ADMIN_PNA}), via {@link RoleCachePort} — mutualisé entre
 * {@code LoginUseCaseImpl}, {@code RefreshTokenUseCaseImpl} et
 * {@code MeUseCaseImpl}.
 */
@Component
public class UserRoleResolver {

    private final RoleCachePort roleCachePort;

    public UserRoleResolver(RoleCachePort roleCachePort) {
        this.roleCachePort = roleCachePort;
    }

    public Set<String> resoudreCodes(Set<UUID> roleIds) {
        return roleCachePort.findAllById(roleIds).stream()
                .map(role -> role.code())
                .collect(Collectors.toUnmodifiableSet());
    }
}
