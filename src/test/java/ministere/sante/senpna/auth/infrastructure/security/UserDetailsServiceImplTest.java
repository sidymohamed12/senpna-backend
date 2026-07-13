package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.infrastructure.cache.RoleCache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl — chargement Spring Security de l'utilisateur")
class UserDetailsServiceImplTest {

    @Mock
    UserRepositoryPort userRepositoryPort;
    @Mock
    RoleCache roleCache;

    UserDetailsServiceImpl sut;

    @BeforeEach
    void setUp() {
        sut = new UserDetailsServiceImpl(userRepositoryPort, roleCache);
    }

    @Test
    @DisplayName("utilisateur trouvé → renvoie un AuthUserPrincipal avec les rôles résolus")
    void utilisateurTrouve_renvoiePrincipal() {
        User user = UserFixtures.actif();
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));
        when(roleCache.findAllById(user.getRoleIds())).thenReturn(
                Set.of(new RoleProjection(UserFixtures.ROLE_GESTIONNAIRE_PNA_ID, "ADMIN_PNA", "Administrateur")));

        UserDetails details = sut.loadUserByUsername(UserFixtures.EMAIL);

        assertThat(details).isInstanceOf(AuthUserPrincipal.class);
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN_PNA");
    }

    @Test
    @DisplayName("utilisateur introuvable → UsernameNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.loadUserByUsername(UserFixtures.EMAIL))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
