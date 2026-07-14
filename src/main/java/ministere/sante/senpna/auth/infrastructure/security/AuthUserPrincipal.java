package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapte {@link User} (domaine, partagé via {@code shared}) au contrat
 * {@link UserDetails} attendu par Spring Security. Les autorités sont
 * préfixées {@code ROLE_} par convention Spring Security, à partir des
 * codes de rôle résolus via {@code RoleCache} (jamais une requête SQL par
 * connexion).
 *
 * <p>
 * Implémente également {@link CurrentUser} afin que d'autres features
 * (ex. {@code utilisateurs}) puissent récupérer l'identité de l'acteur
 * courant sans jamais dépendre directement de cette classe, spécifique à
 * {@code auth}.
 * </p>
 */
public class AuthUserPrincipal implements UserDetails, CurrentUser {

    private final transient User user;
    private final Set<String> roleCodes;
    private UUID entrepotId;
    private UUID structureSanitaireId;
    private UUID fournisseurId;

    public AuthUserPrincipal(User user, Set<String> roleCodes) {
        this.user = user;
        this.roleCodes = roleCodes;
    }

    public User getUser() {
        return user;
    }

    @Override
    public UUID getUserId() {
        return user.getId().getValue();
    }

    public Set<String> getRoleCodes() {
        return roleCodes;
    }

    @Override
    public UUID getEntrepotId() {
        return entrepotId;
    }

    public void setEntrepotId(UUID entrepotId) {
        this.entrepotId = entrepotId;
    }

    @Override
    public UUID getStructureSanitaireId() {
        return structureSanitaireId;
    }

    public void setStructureSanitaireId(UUID structureSanitaireId) {
        this.structureSanitaireId = structureSanitaireId;
    }

    @Override
    public UUID getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(UUID fournisseurId) {
        this.fournisseurId = fournisseurId;
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
