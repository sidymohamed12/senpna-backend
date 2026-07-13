package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;
import ministere.sante.senpna.auth.domain.port.out.ResetTokenPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetPasswordUseCaseImpl — finalisation du changement de mot de passe")
class ResetPasswordUseCaseImplTest {

    @Mock
    UserRepositoryPort userRepositoryPort;
    @Mock
    ResetTokenPort resetTokenPort;
    @Mock
    PasswordEncoderPort passwordEncoderPort;

    ResetPasswordUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ResetPasswordUseCaseImpl(userRepositoryPort, resetTokenPort, passwordEncoderPort);
    }

    @Test
    @DisplayName("consomme le reset token avant toute autre opération")
    void consommeResetTokenEnPremier() {
        User user = UserFixtures.actif();
        when(resetTokenPort.validerEtExtraireUserId("token")).thenReturn(UserFixtures.USER_ID);
        when(userRepositoryPort.findById(UserId.of(UserFixtures.USER_ID))).thenReturn(Optional.of(user));
        when(passwordEncoderPort.encoder("NouveauMdp@2024")).thenReturn("hash-encode");

        sut.reinitialiser(new ResetPasswordCommand("token", "NouveauMdp@2024"));

        assertThat(user.getHashedPassword().value()).isEqualTo("hash-encode");
    }

    @Test
    @DisplayName("reset token invalide → l'exception se propage, aucune recherche utilisateur")
    void resetTokenInvalide_sePropage() {
        when(resetTokenPort.validerEtExtraireUserId("invalide"))
                .thenThrow(new ministere.sante.senpna.auth.domain.exception.ResetTokenInvalideException());

        assertThatThrownBy(() -> sut.reinitialiser(new ResetPasswordCommand("invalide", "Mdp@2024")))
                .isInstanceOf(ministere.sante.senpna.auth.domain.exception.ResetTokenInvalideException.class);
    }

    @Test
    @DisplayName("utilisateur introuvable → UserNotFoundException")
    void utilisateurIntrouvable_leveException() {
        when(resetTokenPort.validerEtExtraireUserId("token")).thenReturn(UUID.randomUUID());
        when(userRepositoryPort.findById(org.mockito.ArgumentMatchers.any())).thenReturn(Optional.empty());

        var resetPasswordCommand = new ResetPasswordCommand("token", "Mdp@2024");
        assertThatThrownBy(() -> sut.reinitialiser(resetPasswordCommand))
                .isInstanceOf(UserNotFoundException.class);
    }
}
