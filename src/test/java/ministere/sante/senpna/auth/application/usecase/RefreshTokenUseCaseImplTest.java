package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.AuthTokenFactory;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;
import ministere.sante.senpna.auth.domain.exception.CompteInactifException;
import ministere.sante.senpna.auth.domain.exception.InvalidRefreshTokenException;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCaseImpl — rotation du refresh token")
class RefreshTokenUseCaseImplTest {

    @Mock
    TokenPort tokenPort;
    @Mock
    UserRepositoryPort userRepositoryPort;
    @Mock
    AuthTokenFactory authTokenFactory;
    @Mock
    UserRoleResolver userRoleResolver;

    RefreshTokenUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new RefreshTokenUseCaseImpl(tokenPort, userRepositoryPort, authTokenFactory, userRoleResolver);
    }

    @Test
    @DisplayName("token révoqué (liste noire) → InvalidRefreshTokenException")
    void tokenRevoque_leveException() {
        when(tokenPort.estInvalide("token")).thenReturn(true);

        var refreshTokenCommand = new RefreshTokenCommand("token");
        assertThatThrownBy(() -> sut.rafraichir(refreshTokenCommand))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("token n'est pas un refresh token → InvalidRefreshTokenException")
    void pasUnRefreshToken_leveException() {
        when(tokenPort.estInvalide("token")).thenReturn(false);
        when(tokenPort.estRefreshToken("token")).thenReturn(false);

        var refreshTokenCommand = new RefreshTokenCommand("token");
        assertThatThrownBy(() -> sut.rafraichir(refreshTokenCommand))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("token expiré → InvalidRefreshTokenException")
    void tokenExpire_leveException() {
        when(tokenPort.estInvalide("token")).thenReturn(false);
        when(tokenPort.estRefreshToken("token")).thenReturn(true);
        when(tokenPort.estExpire("token")).thenReturn(true);

        var refreshTokenCommand = new RefreshTokenCommand("token");
        assertThatThrownBy(() -> sut.rafraichir(refreshTokenCommand))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("utilisateur du token introuvable → InvalidRefreshTokenException")
    void utilisateurIntrouvable_leveException() {
        when(tokenPort.estInvalide("token")).thenReturn(false);
        when(tokenPort.estRefreshToken("token")).thenReturn(true);
        when(tokenPort.estExpire("token")).thenReturn(false);
        when(tokenPort.extraireEmail("token")).thenReturn(UserFixtures.EMAIL);
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.empty());

        var refreshTokenCommand = new RefreshTokenCommand("token");
        assertThatThrownBy(() -> sut.rafraichir(refreshTokenCommand))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("compte désactivé → CompteInactifException")
    void compteDesactive_leveException() {
        User user = UserFixtures.inactif();
        when(tokenPort.estInvalide("token")).thenReturn(false);
        when(tokenPort.estRefreshToken("token")).thenReturn(true);
        when(tokenPort.estExpire("token")).thenReturn(false);
        when(tokenPort.extraireEmail("token")).thenReturn(UserFixtures.EMAIL);
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));

        var refreshTokenCommand = new RefreshTokenCommand("token");
        assertThatThrownBy(() -> sut.rafraichir(refreshTokenCommand))
                .isInstanceOf(CompteInactifException.class);
    }

    @Test
    @DisplayName("succès → révoque l'ancien refresh token puis émet une nouvelle paire")
    void succes_revoqueEtEmetNouvellePaire() {
        User user = UserFixtures.actif();
        when(tokenPort.estInvalide("token")).thenReturn(false);
        when(tokenPort.estRefreshToken("token")).thenReturn(true);
        when(tokenPort.estExpire("token")).thenReturn(false);
        when(tokenPort.extraireEmail("token")).thenReturn(UserFixtures.EMAIL);
        when(userRepositoryPort.findByEmail(Email.of(UserFixtures.EMAIL))).thenReturn(Optional.of(user));
        when(userRoleResolver.resoudreCodes(user.getRoleIds())).thenReturn(Set.of("ADMIN_PNA"));
        AuthTokens nouveauxTokens = new AuthTokens("new-access", "new-refresh", 900L);
        when(authTokenFactory.build(user, Set.of("ADMIN_PNA"))).thenReturn(nouveauxTokens);

        var refreshTokenCommand = new RefreshTokenCommand("token");
        AuthTokens result = sut.rafraichir(refreshTokenCommand);

        assertThat(result).isEqualTo(nouveauxTokens);
        verify(tokenPort).invalider("token");
    }

    @Test
    @DisplayName("JwtValidationException levée en cours de traitement → InvalidRefreshTokenException")
    void jwtException_convertieEnInvalidRefreshToken() {
        when(tokenPort.estInvalide("token"))
                .thenThrow(new com.sidymohamed12.jwt.core.exception.JwtValidationException("expiré"));

        var refreshTokenCommand = new RefreshTokenCommand("token");
        assertThatThrownBy(() -> sut.rafraichir(refreshTokenCommand))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
