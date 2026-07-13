package ministere.sante.senpna.auth.infrastructure.security;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthUserPrincipal — adaptation de User au contrat Spring Security UserDetails")
class AuthUserPrincipalTest {

    @Nested
    @DisplayName("getAuthorities()")
    class GetAuthorities {

        @Test
        @DisplayName("préfixe chaque code de rôle par \"ROLE_\"")
        void prefixeChaqueRole() {
            AuthUserPrincipal sut = new AuthUserPrincipal(UserFixtures.actif(), Set.of("ADMIN_PNA", "PHARMACIEN"));

            assertThat(sut.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_ADMIN_PNA", "ROLE_PHARMACIEN");
        }

        @Test
        @DisplayName("aucun rôle → aucune autorité")
        void aucunRole_aucuneAutorite() {
            AuthUserPrincipal sut = new AuthUserPrincipal(UserFixtures.actif(), Set.of());

            assertThat(sut.getAuthorities()).isEmpty();
        }
    }

    @Nested
    @DisplayName("délégation à l'agrégat User")
    class DelegationVersUser {

        @Test
        @DisplayName("getUsername() renvoie l'email, getPassword() le hash")
        void usernameEtPassword() {
            AuthUserPrincipal sut = new AuthUserPrincipal(UserFixtures.actif(), Set.of());

            assertThat(sut.getUsername()).isEqualTo(UserFixtures.EMAIL);
            assertThat(sut.getPassword()).isEqualTo(UserFixtures.PASSWORD_HASH);
        }

        @Test
        @DisplayName("isEnabled() reflète le statut actif de l'utilisateur")
        void isEnabled_refleteStatutActif() {
            assertThat(new AuthUserPrincipal(UserFixtures.actif(), Set.of()).isEnabled()).isTrue();
            assertThat(new AuthUserPrincipal(UserFixtures.inactif(), Set.of()).isEnabled()).isFalse();
        }

        @Test
        @DisplayName("isAccountNonLocked() reflète le verrouillage de l'utilisateur")
        void isAccountNonLocked_refleteVerrouillage() {
            assertThat(new AuthUserPrincipal(UserFixtures.actif(), Set.of()).isAccountNonLocked()).isTrue();
            assertThat(new AuthUserPrincipal(UserFixtures.verrouille(), Set.of()).isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("isAccountNonExpired() et isCredentialsNonExpired() toujours vrais")
        void toujoursVrais() {
            AuthUserPrincipal sut = new AuthUserPrincipal(UserFixtures.actif(), Set.of());

            assertThat(sut.isAccountNonExpired()).isTrue();
            assertThat(sut.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("getUserId() délègue à l'identifiant de l'utilisateur")
        void getUserId_delegue() {
            AuthUserPrincipal sut = new AuthUserPrincipal(UserFixtures.actif(), Set.of());

            assertThat(sut.getUserId()).isEqualTo(UserFixtures.USER_ID);
        }
    }

    @Nested
    @DisplayName("affectation organisationnelle mutable")
    class AffectationOrganisationnelle {

        @Test
        @DisplayName("entrepotId et structureSanitaireId sont null par défaut")
        void nullParDefaut() {
            AuthUserPrincipal sut = new AuthUserPrincipal(UserFixtures.actif(), Set.of());

            assertThat(sut.getEntrepotId()).isNull();
            assertThat(sut.getStructureSanitaireId()).isNull();
        }

        @Test
        @DisplayName("peuvent être définis après construction (cf. JwtAuthenticationFilter)")
        void peuventEtreDefinis() {
            AuthUserPrincipal sut = new AuthUserPrincipal(UserFixtures.actif(), Set.of());
            UUID entrepotId = UUID.randomUUID();

            sut.setEntrepotId(entrepotId);

            assertThat(sut.getEntrepotId()).isEqualTo(entrepotId);
        }
    }

    @Test
    @DisplayName("getUser() renvoie l'agrégat User sous-jacent")
    void getUser_renvoieUser() {
        User user = UserFixtures.actif();
        AuthUserPrincipal sut = new AuthUserPrincipal(user, Set.of());

        assertThat(sut.getUser()).isSameAs(user);
    }
}
