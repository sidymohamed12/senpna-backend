package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.auth.domain.model.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapte {@link User} (domaine) au contrat {@link UserDetails} attendu par
 * Spring Security. Les autorités sont préfixées {@code ROLE_} par
 * convention Spring Security, à partir des codes de rôle résolus via
 * {@code RoleCache} (jamais une requête SQL par connexion).
 */
public class AuthUserPrincipal implements UserDetails {

    private final User user;
    private final Set<String> roleCodes;

    public AuthUserPrincipal(User user, Set<String> roleCodes) {
        this.user = user;
        this.roleCodes = roleCodes;
    }

    public User getUser() {
        return user;
    }

    public Set<String> getRoleCodes() {
        return roleCodes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleCodes.stream()
                .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return user.getHashedPassword().value();
    }

    @Override
    public String getUsername() {
        return user.getEmail().value();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.estVerrouille();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActif();
    }
}
