package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.OtpService;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.auth.domain.port.out.ResetTokenPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyOtpUseCaseImpl — vérification du code OTP et émission du reset token")
class VerifyOtpUseCaseImplTest {

    @Mock
    OtpService otpService;
    @Mock
    ResetTokenPort resetTokenPort;
    @Mock
    UserRepositoryPort userRepositoryPort;

    VerifyOtpUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new VerifyOtpUseCaseImpl(otpService, resetTokenPort, userRepositoryPort);
    }

    @Test
    @DisplayName("valide le code AVANT de résoudre l'utilisateur")
    void valideCodeAvantResolutionUtilisateur() {
        User user = UserFixtures.actif();
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));
        when(resetTokenPort.genererResetToken(UserFixtures.USER_ID, UserFixtures.EMAIL)).thenReturn("reset-token");

        sut.verifier(new VerifyOtpCommand(UserFixtures.EMAIL, "123456"));

        InOrder ordre = inOrder(otpService, userRepositoryPort);
        ordre.verify(otpService).valider(UserFixtures.EMAIL, "123456");
        ordre.verify(userRepositoryPort).findByEmail(Email.of(UserFixtures.EMAIL));
    }

    @Test
    @DisplayName("code invalide → l'exception du service OTP se propage, aucun reset token émis")
    void codeInvalide_sePropage() {
        doThrow(new ministere.sante.senpna.auth.domain.exception.OtpInvalideException())
                .when(otpService).valider(UserFixtures.EMAIL, "000000");

        assertThatThrownBy(() -> sut.verifier(new VerifyOtpCommand(UserFixtures.EMAIL, "000000")))
                .isInstanceOf(ministere.sante.senpna.auth.domain.exception.OtpInvalideException.class);

        verify(resetTokenPort, never()).genererResetToken(any(), any());
    }

    @Test
    @DisplayName("code valide mais utilisateur introuvable → UserNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.empty());

        var verifyOtpCommand = new VerifyOtpCommand(UserFixtures.EMAIL, "123456");
        assertThatThrownBy(() -> sut.verifier(verifyOtpCommand))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("succès → renvoie le reset token généré")
    void succes_renvoieResetToken() {
        User user = UserFixtures.actif();
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));
        when(resetTokenPort.genererResetToken(UserFixtures.USER_ID, UserFixtures.EMAIL)).thenReturn("reset-token");

        VerifyOtpResult result = sut.verifier(new VerifyOtpCommand(UserFixtures.EMAIL, "123456"));

        assertThat(result).isEqualTo(new VerifyOtpResult("reset-token"));
    }
}
