package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.command.AuthCommands.LogoutCommand;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUseCaseImpl — révocation des tokens à la déconnexion")
class LogoutUseCaseImplTest {

    @Mock
    TokenPort tokenPort;

    LogoutUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new LogoutUseCaseImpl(tokenPort);
    }

    @Test
    @DisplayName("révoque l'access token et le refresh token quand les deux sont fournis")
    void revoqueLesDeuxTokens() {
        sut.logout(new LogoutCommand("access", "refresh"));

        verify(tokenPort).invalider("access");
        verify(tokenPort).invalider("refresh");
    }

    @Test
    @DisplayName("refresh token absent (null) → seul l'access token est révoqué")
    void refreshTokenAbsent_seulAccessRevoque() {
        sut.logout(new LogoutCommand("access", null));

        verify(tokenPort).invalider("access");
        verify(tokenPort, never()).invalider(null);
    }

    @Test
    @DisplayName("refresh token blanc → ignoré, non transmis au port")
    void refreshTokenBlanc_ignore() {
        sut.logout(new LogoutCommand("access", "   "));

        verify(tokenPort).invalider("access");
        verify(tokenPort, never()).invalider("   ");
    }

    @Test
    @DisplayName("révocation en erreur (token malformé) → ne fait jamais échouer le logout")
    void erreurRevocation_neFaitJamaisEchouerLogout() {
        doThrow(new RuntimeException("token malformé")).when(tokenPort).invalider("access");

        assertThatCode(() -> sut.logout(new LogoutCommand("access", "refresh"))).doesNotThrowAnyException();

        verify(tokenPort).invalider("refresh");
    }
}
