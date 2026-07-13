package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;
import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.config.AppProperties.JwtProperties;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenFactory — construction de la paire de tokens")
class AuthTokenFactoryTest {

    @Mock
    TokenPort tokenPort;
    @Mock
    UserAffectationResolver userAffectationResolver;

    AuthTokenFactory sut;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(
                new JwtProperties("secret", Duration.ofMinutes(15), Duration.ofDays(7)),
                null, null, null, null, null);
        sut = new AuthTokenFactory(tokenPort, appProperties, userAffectationResolver);
    }

    @Test
    @DisplayName("embarque l'affectation organisationnelle résolue dans l'access token")
    void embarqueAffectation() {
        User user = UserFixtures.actif();
        UUID entrepotId = UUID.randomUUID();
        when(userAffectationResolver.resoudre(UserFixtures.USER_ID))
                .thenReturn(new UserAffectationView(UserFixtures.USER_ID, entrepotId, null));
        when(tokenPort.genererAccess(eq(user), any(), eq(entrepotId), isNull())).thenReturn("access-token");
        when(tokenPort.genererRefresh(user)).thenReturn("refresh-token");

        AuthTokens tokens = sut.build(user, Set.of("ADMIN_PNA"));

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("expiresInSeconds correspond au TTL configuré, converti en secondes")
    void expiresInSecondsDepuisTtlConfigure() {
        User user = UserFixtures.actif();
        when(userAffectationResolver.resoudre(any()))
                .thenReturn(new UserAffectationView(UserFixtures.USER_ID, null, null));
        when(tokenPort.genererAccess(any(), any(), any(), any())).thenReturn("access-token");
        when(tokenPort.genererRefresh(any())).thenReturn("refresh-token");

        AuthTokens tokens = sut.build(user, Set.of());

        assertThat(tokens.expiresInSeconds()).isEqualTo(15 * 60L);
    }
}
