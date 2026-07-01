package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.infrastructure.cache.RoleCache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Services applicatifs — OtpDestinationResolver / AuthTokenFactory / UserRoleResolver")
class ApplicationServicesTest {

    // ══════════════════════════════════════════════════════════════════════
    // OtpDestinationResolver
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("OtpDestinationResolver")
    class OtpDestinationResolverTest {

        OtpDestinationResolver sut = new OtpDestinationResolver();

        @Test
        @DisplayName("canal EMAIL → retourne l'email de l'utilisateur")
        void resoudre_email_retourne_email() {
            User user = UserFixtures.actif();
            String destination = sut.resoudre(user, OtpChannel.EMAIL);
            assertThat(destination).isEqualTo(UserFixtures.EMAIL);
        }

        @Test
        @DisplayName("canal SMS avec téléphone → retourne le numéro")
        void resoudre_sms_avec_telephone_retourne_numero() {
            User user = UserFixtures.actifAvecTelephone();
            String destination = sut.resoudre(user, OtpChannel.SMS);
            assertThat(destination).isEqualTo(UserFixtures.TELEPHONE);
        }

        @Test
        @DisplayName("canal SMS sans téléphone → BusinessRuleException avec code NO_PHONE_REGISTERED")
        void resoudre_sms_sans_telephone_leve_exception() {
            User user = UserFixtures.actif(); // pas de téléphone

            assertThatThrownBy(() -> sut.resoudre(user, OtpChannel.SMS))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("téléphone");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // AuthTokenFactory
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("AuthTokenFactory")
    class AuthTokenFactoryTest {

        @Mock
        TokenPort tokenPort;
        @Mock
        AppProperties appProperties;
        @InjectMocks
        AuthTokenFactory sut;

        @Test
        @DisplayName("build() génère access + refresh et utilise le TTL de AppProperties")
        void build_retourne_auth_tokens_corrects() {
            User user = UserFixtures.actif();
            Set<String> roleCodes = Set.of("GESTIONNAIRE_PNA");
            AppProperties.JwtProperties jwt = mock(AppProperties.JwtProperties.class);

            when(tokenPort.genererAccess(user, roleCodes)).thenReturn("access.jwt");
            when(tokenPort.genererRefresh(user)).thenReturn("refresh.jwt");
            when(appProperties.jwt()).thenReturn(jwt);
            when(jwt.accessTokenTtl()).thenReturn(Duration.ofHours(1));

            AuthTokens tokens = sut.build(user, roleCodes);

            assertThat(tokens.accessToken()).isEqualTo("access.jwt");
            assertThat(tokens.refreshToken()).isEqualTo("refresh.jwt");
            assertThat(tokens.expiresInSeconds()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("build() appelle genererAccess avec l'utilisateur et les rôles")
        void build_appelle_genererAccess_avec_bons_args() {
            User user = UserFixtures.actif();
            Set<String> roleCodes = Set.of("ADMIN_PNA");
            AppProperties.JwtProperties jwt = mock(AppProperties.JwtProperties.class);
            when(tokenPort.genererAccess(any(), any())).thenReturn("tok");
            when(tokenPort.genererRefresh(any())).thenReturn("ref");
            when(appProperties.jwt()).thenReturn(jwt);
            when(jwt.accessTokenTtl()).thenReturn(Duration.ofMinutes(30));

            sut.build(user, roleCodes);

            verify(tokenPort).genererAccess(user, roleCodes);
            verify(tokenPort).genererRefresh(user);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UserRoleResolver
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UserRoleResolver")
    class UserRoleResolverTest {

        @Mock
        RoleCache roleCache;
        @InjectMocks
        UserRoleResolver sut;

        @Test
        @DisplayName("résout les codes de rôle depuis le cache pour un utilisateur multi-rôles")
        void resoudreCodes_multi_roles() {
            UUID roleId1 = UserFixtures.ROLE_GESTIONNAIRE_PNA_ID;
            UUID roleId2 = UserFixtures.ROLE_PHARMACIEN_PRA_ID;

            when(roleCache.findAllById(Set.of(roleId1, roleId2))).thenReturn(Set.of(
                    new RoleProjection(roleId1, "GESTIONNAIRE_PNA", "Gestionnaire PNA"),
                    new RoleProjection(roleId2, "PHARMACIEN_PRA", "Pharmacien PRA")));

            Set<String> codes = sut.resoudreCodes(Set.of(roleId1, roleId2));

            assertThat(codes).containsExactlyInAnyOrder("GESTIONNAIRE_PNA", "PHARMACIEN_PRA");
        }

        @Test
        @DisplayName("ensemble vide de rôles → retourne ensemble vide")
        void resoudreCodes_vide_retourne_vide() {
            when(roleCache.findAllById(Set.of())).thenReturn(Set.of());
            assertThat(sut.resoudreCodes(Set.of())).isEmpty();
        }

        @Test
        @DisplayName("résultat est non modifiable")
        void resoudreCodes_retourne_non_modifiable() {
            when(roleCache.findAllById(any())).thenReturn(Set.of(
                    new RoleProjection(UUID.randomUUID(), "ADMIN_PNA", "Admin PNA")));

            Set<String> codes = sut.resoudreCodes(Set.of(UUID.randomUUID()));

            assertThatThrownBy(() -> codes.add("EXTRA"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
