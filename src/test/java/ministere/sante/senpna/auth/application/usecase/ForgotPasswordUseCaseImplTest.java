package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.OtpDestinationResolver;
import ministere.sante.senpna.auth.application.service.OtpService;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ForgotPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResendOtpCommand;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForgotPasswordUseCaseImpl / ResendOtpUseCaseImpl — envoi d'un code OTP")
class ForgotPasswordUseCaseImplTest {

    @Mock
    UserRepositoryPort userRepositoryPort;
    @Mock
    OtpService otpService;
    @Mock
    OtpDestinationResolver otpDestinationResolver;

    @Nested
    @DisplayName("ForgotPasswordUseCaseImpl")
    class ForgotPassword {

        ForgotPasswordUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ForgotPasswordUseCaseImpl(userRepositoryPort, otpService, otpDestinationResolver);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void utilisateurIntrouvable_leveException() {
            when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.demander(new ForgotPasswordCommand(UserFixtures.EMAIL, OtpChannel.EMAIL)))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("résout la destination puis génère et envoie l'OTP")
        void resoutDestinationEtEnvoie() {
            User user = UserFixtures.actif();
            when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));
            when(otpDestinationResolver.resoudre(user, OtpChannel.EMAIL)).thenReturn(UserFixtures.EMAIL);

            sut.demander(new ForgotPasswordCommand(UserFixtures.EMAIL, OtpChannel.EMAIL));

            verify(otpService).genererEtEnvoyer(UserFixtures.EMAIL, OtpChannel.EMAIL, UserFixtures.EMAIL);
        }
    }

    @Nested
    @DisplayName("ResendOtpUseCaseImpl")
    class ResendOtp {

        ResendOtpUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new ResendOtpUseCaseImpl(userRepositoryPort, otpService, otpDestinationResolver);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void utilisateurIntrouvable_leveException() {
            when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.renvoyer(new ResendOtpCommand(UserFixtures.EMAIL, OtpChannel.SMS)))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("résout la destination puis régénère et renvoie l'OTP")
        void resoutDestinationEtRenvoie() {
            User user = UserFixtures.actifAvecTelephone();
            when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));
            when(otpDestinationResolver.resoudre(user, OtpChannel.SMS)).thenReturn(UserFixtures.TELEPHONE);

            sut.renvoyer(new ResendOtpCommand(UserFixtures.EMAIL, OtpChannel.SMS));

            verify(otpService).genererEtEnvoyer(UserFixtures.EMAIL, OtpChannel.SMS, UserFixtures.TELEPHONE);
        }
    }
}
