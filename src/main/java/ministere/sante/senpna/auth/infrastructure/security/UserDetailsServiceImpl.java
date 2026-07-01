package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.infrastructure.cache.RoleCache;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation Spring Security du chargement utilisateur — invoquée par
 * {@code JwtAuthenticationFilter} à chaque requête authentifiée.
 *
 * <p>
 * Les rôles sont résolus via {@link RoleCache} (mémoire, chargé au
 * démarrage) — aucune requête SQL supplémentaire vers la table {@code roles}.
 * </p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleCache roleCache;

    public UserDetailsServiceImpl(UserRepositoryPort userRepositoryPort, RoleCache roleCache) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleCache = roleCache;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepositoryPort.findByEmail(Email.of(email))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

        Set<String> roleCodes = resoudreCodesRoles(user.getRoleIds());
        return new AuthUserPrincipal(user, roleCodes);
    }

    private Set<String> resoudreCodesRoles(Set<UUID> roleIds) {
        return roleCache.findAllById(roleIds).stream()
                .map(role -> role.code())
                .collect(Collectors.toUnmodifiableSet());
    }
}
