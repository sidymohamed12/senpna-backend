package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.OtpDestinationResolver;
import ministere.sante.senpna.auth.application.service.OtpService;
import ministere.sante.senpna.auth.application.service.UserAffectationResolver;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.*;
import ministere.sante.senpna.auth.domain.port.out.*;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Auth Use Cases — forgot-password / resend / verify / reset / refresh / me")
class AuthUseCasesTest {

    // ══════════════════════════════════════════════════════════════════════
    // ResendOtpUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ResendOtpUseCaseImpl")
    class ResendOtpTest {

        @Mock
        UserRepositoryPort userRepositoryPort;
        @Mock
        OtpService otpService;
        @Mock
        OtpDestinationResolver otpDestinationResolver;
        @InjectMocks
        ResendOtpUseCaseImpl sut;

        @Test
        @DisplayName("redirige vers OtpService avec la bonne destination")
        void renvoyer_appelle_otp_service() {
            User user = UserFixtures.actif();
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.of(user));
            when(otpDestinationResolver.resoudre(user, OtpChannel.EMAIL))
                    .thenReturn(UserFixtures.EMAIL);

            sut.renvoyer(new ResendOtpCommand(UserFixtures.EMAIL, OtpChannel.EMAIL));

            verify(otpService).genererEtEnvoyer(UserFixtures.EMAIL, OtpChannel.EMAIL, UserFixtures.EMAIL);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void renvoyer_user_absent_leve_exception() {
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());

            var resendOtpCommand = new ResendOtpCommand(UserFixtures.EMAIL, OtpChannel.EMAIL);
            assertThatThrownBy(() -> sut.renvoyer(resendOtpCommand))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(otpService);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MeUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("MeUseCaseImpl")
    class MeTest {

        @Mock
        UserRepositoryPort userRepositoryPort;
        @Mock
        UserRoleResolver userRoleResolver;
        @Mock
        UserAffectationResolver userAffectationResolver;
        @InjectMocks
        MeUseCaseImpl sut;

        @Test
        @DisplayName("retourne le UserSummary complet de l'utilisateur connecté")
        void me_retourne_summary() {
            User user = UserFixtures.actif();
            UUID entrepotId = UUID.randomUUID();
            when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL)))
                    .thenReturn(Optional.of(user));
            when(userRoleResolver.resoudreCodes(any())).thenReturn(Set.of("GESTIONNAIRE_PNA"));
            when(userAffectationResolver.resoudre(user.getId().getValue()))
                    .thenReturn(new UserAffectationView(user.getId().getValue(), entrepotId, null));

            UserSummary result = sut.me(new MeQuery(UserFixtures.EMAIL));

            assertThat(result.id()).isEqualTo(UserFixtures.USER_ID);
            assertThat(result.nom()).isEqualTo(UserFixtures.NOM);
            assertThat(result.prenom()).isEqualTo(UserFixtures.PRENOM);
            assertThat(result.email()).isEqualTo(UserFixtures.EMAIL);
            assertThat(result.roles()).containsExactly("GESTIONNAIRE_PNA");
            assertThat(result.entrepotId()).isEqualTo(entrepotId);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void me_user_absent_leve_exception() {
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());

            var meQuery = new MeQuery(UserFixtures.EMAIL);
            assertThatThrownBy(() -> sut.me(meQuery))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(userAffectationResolver);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LogoutUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("LogoutUseCaseImpl")
    class LogoutTest {

        private static final String ACCESS = "access-token";
        private static final String REFRESH = "refresh-token";

        @Mock
        TokenPort tokenPort;
        @InjectMocks
        LogoutUseCaseImpl sut;

        @Test
        @DisplayName("révoque l'access token et le refresh token fournis")
        void logout_revoque_access_et_refresh() {
            sut.logout(new LogoutCommand(ACCESS, REFRESH));

            verify(tokenPort).invalider(ACCESS);
            verify(tokenPort).invalider(REFRESH);
        }

        @Test
        @DisplayName("refresh token absent — ne révoque que l'access token")
        void logout_sans_refresh_token() {
            sut.logout(new LogoutCommand(ACCESS, null));

            verify(tokenPort).invalider(ACCESS);
            verify(tokenPort, times(1)).invalider(anyString());
        }

        @Test
        @DisplayName("access token malformé — n'échoue pas, révoque quand même le refresh token")
        void logout_access_token_invalide_ne_leve_pas() {
            doThrow(new JwtException("token malformé")).when(tokenPort).invalider(ACCESS);

            sut.logout(new LogoutCommand(ACCESS, REFRESH));

            verify(tokenPort).invalider(REFRESH);
        }
    }
}