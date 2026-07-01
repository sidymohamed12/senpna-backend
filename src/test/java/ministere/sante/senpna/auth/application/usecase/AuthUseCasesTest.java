package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.AuthTokenFactory;
import ministere.sante.senpna.auth.application.service.OtpDestinationResolver;
import ministere.sante.senpna.auth.application.service.OtpService;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.*;
import ministere.sante.senpna.auth.domain.exception.*;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.out.*;
import ministere.sante.senpna.auth.domain.valueobject.HashedPassword;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.auth.domain.valueobject.UserId;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.infrastructure.exception.BusinessRuleException;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ══════════════════════════════════════════════════════════════════════════════
// ForgotPasswordUseCaseImpl
// ══════════════════════════════════════════════════════════════════════════════

@DisplayName("Auth Use Cases — forgot-password / resend / verify / reset / refresh / me")
class AuthUseCasesTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ForgotPasswordUseCaseImpl")
    class ForgotPasswordTest {

        @Mock UserRepositoryPort userRepositoryPort;
        @Mock OtpService otpService;
        @Mock OtpDestinationResolver otpDestinationResolver;
        @InjectMocks ForgotPasswordUseCaseImpl sut;

        @Test
        @DisplayName("canal EMAIL — récupère l'email comme destination et envoie l'OTP")
        void demander_email_canal_envoi_otp() {
            User user = UserFixtures.actif();
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.of(user));
            when(otpDestinationResolver.resoudre(user, OtpChannel.EMAIL))
                    .thenReturn(UserFixtures.EMAIL);

            sut.demander(new ForgotPasswordCommand(UserFixtures.EMAIL, OtpChannel.EMAIL));

            verify(otpService).genererEtEnvoyer(UserFixtures.EMAIL, OtpChannel.EMAIL, UserFixtures.EMAIL);
        }

        @Test
        @DisplayName("canal SMS — récupère le téléphone comme destination et envoie l'OTP")
        void demander_sms_canal_envoi_otp() {
            User user = UserFixtures.actifAvecTelephone();
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.of(user));
            when(otpDestinationResolver.resoudre(user, OtpChannel.SMS))
                    .thenReturn(UserFixtures.TELEPHONE);

            sut.demander(new ForgotPasswordCommand(UserFixtures.EMAIL, OtpChannel.SMS));

            verify(otpService).genererEtEnvoyer(UserFixtures.EMAIL, OtpChannel.SMS, UserFixtures.TELEPHONE);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void demander_user_absent_leve_exception() {
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.demander(
                    new ForgotPasswordCommand(UserFixtures.EMAIL, OtpChannel.EMAIL)))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(otpService);
        }

        @Test
        @DisplayName("SMS sans téléphone → BusinessRuleException propagée depuis le resolver")
        void demander_sms_sans_telephone_leve_exception() {
            User user = UserFixtures.actif(); // sans téléphone
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.of(user));
            when(otpDestinationResolver.resoudre(user, OtpChannel.SMS))
                    .thenThrow(new BusinessRuleException("Aucun téléphone", "NO_PHONE_REGISTERED"));

            assertThatThrownBy(() -> sut.demander(
                    new ForgotPasswordCommand(UserFixtures.EMAIL, OtpChannel.SMS)))
                    .isInstanceOf(BusinessRuleException.class);

            verifyNoInteractions(otpService);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ResendOtpUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ResendOtpUseCaseImpl")
    class ResendOtpTest {

        @Mock UserRepositoryPort userRepositoryPort;
        @Mock OtpService otpService;
        @Mock OtpDestinationResolver otpDestinationResolver;
        @InjectMocks ResendOtpUseCaseImpl sut;

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

            assertThatThrownBy(() -> sut.renvoyer(
                    new ResendOtpCommand(UserFixtures.EMAIL, OtpChannel.EMAIL)))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(otpService);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // VerifyOtpUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("VerifyOtpUseCaseImpl")
    class VerifyOtpTest {

        @Mock OtpService otpService;
        @Mock ResetTokenPort resetTokenPort;
        @Mock UserRepositoryPort userRepositoryPort;
        @InjectMocks VerifyOtpUseCaseImpl sut;

        @Test
        @DisplayName("succès — valide OTP, génère un reset token et le retourne")
        void verifier_succes_retourne_reset_token() {
            User user = UserFixtures.actif();
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.of(user));
            when(resetTokenPort.genererResetToken(UserFixtures.USER_ID, UserFixtures.EMAIL))
                    .thenReturn("reset-token-xyz");

            VerifyOtpResult result = sut.verifier(new VerifyOtpCommand(UserFixtures.EMAIL, "123456"));

            verify(otpService).valider(UserFixtures.EMAIL, "123456");
            verify(resetTokenPort).genererResetToken(UserFixtures.USER_ID, UserFixtures.EMAIL);
            assertThat(result.resetToken()).isEqualTo("reset-token-xyz");
        }

        @Test
        @DisplayName("OTP invalide — OtpInvalideException propagée sans générer de reset token")
        void verifier_otp_invalide_leve_exception() {
            doThrow(new OtpInvalideException()).when(otpService).valider(anyString(), anyString());

            assertThatThrownBy(() -> sut.verifier(new VerifyOtpCommand(UserFixtures.EMAIL, "000000")))
                    .isInstanceOf(OtpInvalideException.class);

            verifyNoInteractions(resetTokenPort);
        }

        @Test
        @DisplayName("OTP expiré — OtpExpireException propagée")
        void verifier_otp_expire_leve_exception() {
            doThrow(new OtpExpireException()).when(otpService).valider(anyString(), anyString());

            assertThatThrownBy(() -> sut.verifier(new VerifyOtpCommand(UserFixtures.EMAIL, "123456")))
                    .isInstanceOf(OtpExpireException.class);
        }

        @Test
        @DisplayName("OTP valide mais utilisateur introuvable → UserNotFoundException")
        void verifier_otp_ok_user_absent_leve_exception() {
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.verifier(new VerifyOtpCommand(UserFixtures.EMAIL, "123456")))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(resetTokenPort);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ResetPasswordUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ResetPasswordUseCaseImpl")
    class ResetPasswordTest {

        @Mock UserRepositoryPort userRepositoryPort;
        @Mock ResetTokenPort resetTokenPort;
        @Mock PasswordEncoderPort passwordEncoderPort;
        @InjectMocks ResetPasswordUseCaseImpl sut;

        @Test
        @DisplayName("succès — consomme le reset token, encode le mdp et sauvegarde")
        void reinitialiser_succes() {
            when(resetTokenPort.validerEtExtraireUserId("valid-reset-token"))
                    .thenReturn(UserFixtures.USER_ID);
            when(userRepositoryPort.findById(UserId.of(UserFixtures.USER_ID)))
                    .thenReturn(Optional.of(UserFixtures.actif()));
            when(passwordEncoderPort.encoder("NouveauMdp@2024")).thenReturn("$2a$12$newhash");
            when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            sut.reinitialiser(new ResetPasswordCommand("valid-reset-token", "NouveauMdp@2024"));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getHashedPassword().value()).isEqualTo("$2a$12$newhash");
        }

        @Test
        @DisplayName("reset token invalide — ResetTokenInvalideException")
        void reinitialiser_token_invalide_leve_exception() {
            when(resetTokenPort.validerEtExtraireUserId("bad-token"))
                    .thenThrow(new ResetTokenInvalideException());

            assertThatThrownBy(() -> sut.reinitialiser(
                    new ResetPasswordCommand("bad-token", "NouveauMdp@2024")))
                    .isInstanceOf(ResetTokenInvalideException.class);

            verifyNoInteractions(userRepositoryPort, passwordEncoderPort);
        }

        @Test
        @DisplayName("reset token valide mais utilisateur introuvable → UserNotFoundException")
        void reinitialiser_user_absent_leve_exception() {
            when(resetTokenPort.validerEtExtraireUserId(anyString()))
                    .thenReturn(UserFixtures.USER_ID);
            when(userRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.reinitialiser(
                    new ResetPasswordCommand("token", "NouveauMdp@2024")))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(passwordEncoderPort);
        }

        @Test
        @DisplayName("le changement de mot de passe remet les échecs à zéro")
        void reinitialiser_reset_echecs_connexion() {
            when(resetTokenPort.validerEtExtraireUserId(anyString()))
                    .thenReturn(UserFixtures.USER_ID);
            when(userRepositoryPort.findById(any()))
                    .thenReturn(Optional.of(UserFixtures.avecEchecs(3)));
            when(passwordEncoderPort.encoder(anyString())).thenReturn("$2a$12$newhash");
            when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            sut.reinitialiser(new ResetPasswordCommand("token", "NouveauMdp@2024"));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getTentativesEchecConnexion()).isZero();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RefreshTokenUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("RefreshTokenUseCaseImpl")
    class RefreshTokenTest {

        @Mock TokenPort tokenPort;
        @Mock UserRepositoryPort userRepositoryPort;
        @Mock AuthTokenFactory authTokenFactory;
        @Mock UserRoleResolver userRoleResolver;
        @InjectMocks RefreshTokenUseCaseImpl sut;

        private static final String OLD_REFRESH = "old.refresh.token";

        @Test
        @DisplayName("succès — révoque l'ancien token et retourne une nouvelle paire")
        void rafraichir_succes_rotation() {
            AuthTokens newTokens = new AuthTokens("new.access", "new.refresh", 3600L);
            when(tokenPort.estInvalide(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.estRefreshToken(OLD_REFRESH)).thenReturn(true);
            when(tokenPort.estExpire(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.extraireEmail(OLD_REFRESH)).thenReturn(UserFixtures.EMAIL);
            when(userRepositoryPort.findByEmail(any()))
                    .thenReturn(Optional.of(UserFixtures.actif()));
            when(userRoleResolver.resoudreCodes(any())).thenReturn(Set.of("GESTIONNAIRE_PNA"));
            when(authTokenFactory.build(any(User.class), any())).thenReturn(newTokens);

            AuthTokens result = sut.rafraichir(new RefreshTokenCommand(OLD_REFRESH));

            verify(tokenPort).invalider(OLD_REFRESH); // rotation : révocation de l'ancien
            assertThat(result.accessToken()).isEqualTo("new.access");
            assertThat(result.refreshToken()).isEqualTo("new.refresh");
        }

        @Test
        @DisplayName("token déjà révoqué → InvalidRefreshTokenException")
        void rafraichir_token_revoque_leve_exception() {
            when(tokenPort.estInvalide(OLD_REFRESH)).thenReturn(true);

            assertThatThrownBy(() -> sut.rafraichir(new RefreshTokenCommand(OLD_REFRESH)))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verify(tokenPort, never()).invalider(anyString());
        }

        @Test
        @DisplayName("pas un refresh token → InvalidRefreshTokenException")
        void rafraichir_pas_refresh_leve_exception() {
            when(tokenPort.estInvalide(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.estRefreshToken(OLD_REFRESH)).thenReturn(false);

            assertThatThrownBy(() -> sut.rafraichir(new RefreshTokenCommand(OLD_REFRESH)))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("token expiré → InvalidRefreshTokenException")
        void rafraichir_token_expire_leve_exception() {
            when(tokenPort.estInvalide(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.estRefreshToken(OLD_REFRESH)).thenReturn(true);
            when(tokenPort.estExpire(OLD_REFRESH)).thenReturn(true);

            assertThatThrownBy(() -> sut.rafraichir(new RefreshTokenCommand(OLD_REFRESH)))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            verify(tokenPort, never()).invalider(anyString());
        }

        @Test
        @DisplayName("compte inactif → CompteInactifException")
        void rafraichir_compte_inactif_leve_exception() {
            when(tokenPort.estInvalide(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.estRefreshToken(OLD_REFRESH)).thenReturn(true);
            when(tokenPort.estExpire(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.extraireEmail(OLD_REFRESH)).thenReturn(UserFixtures.EMAIL);
            when(userRepositoryPort.findByEmail(any()))
                    .thenReturn(Optional.of(UserFixtures.inactif()));

            assertThatThrownBy(() -> sut.rafraichir(new RefreshTokenCommand(OLD_REFRESH)))
                    .isInstanceOf(CompteInactifException.class);

            verify(tokenPort, never()).invalider(anyString());
        }

        @Test
        @DisplayName("JwtException levée → InvalidRefreshTokenException")
        void rafraichir_jwt_exception_wrappee() {
            when(tokenPort.estInvalide(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.estRefreshToken(OLD_REFRESH))
                    .thenThrow(new JwtException("signature invalide"));

            assertThatThrownBy(() -> sut.rafraichir(new RefreshTokenCommand(OLD_REFRESH)))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        @DisplayName("utilisateur introuvable → InvalidRefreshTokenException (pas UserNotFoundException)")
        void rafraichir_user_absent_leve_invalid_refresh() {
            when(tokenPort.estInvalide(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.estRefreshToken(OLD_REFRESH)).thenReturn(true);
            when(tokenPort.estExpire(OLD_REFRESH)).thenReturn(false);
            when(tokenPort.extraireEmail(OLD_REFRESH)).thenReturn(UserFixtures.EMAIL);
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.rafraichir(new RefreshTokenCommand(OLD_REFRESH)))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MeUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("MeUseCaseImpl")
    class MeTest {

        @Mock UserRepositoryPort userRepositoryPort;
        @Mock UserRoleResolver userRoleResolver;
        @InjectMocks MeUseCaseImpl sut;

        @Test
        @DisplayName("retourne le UserSummary complet de l'utilisateur connecté")
        void me_retourne_summary() {
            User user = UserFixtures.actif();
            when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL)))
                    .thenReturn(Optional.of(user));
            when(userRoleResolver.resoudreCodes(any())).thenReturn(Set.of("GESTIONNAIRE_PNA"));

            UserSummary result = sut.me(new MeQuery(UserFixtures.EMAIL));

            assertThat(result.id()).isEqualTo(UserFixtures.USER_ID);
            assertThat(result.nom()).isEqualTo(UserFixtures.NOM);
            assertThat(result.prenom()).isEqualTo(UserFixtures.PRENOM);
            assertThat(result.email()).isEqualTo(UserFixtures.EMAIL);
            assertThat(result.roles()).containsExactly("GESTIONNAIRE_PNA");
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void me_user_absent_leve_exception() {
            when(userRepositoryPort.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.me(new MeQuery(UserFixtures.EMAIL)))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
