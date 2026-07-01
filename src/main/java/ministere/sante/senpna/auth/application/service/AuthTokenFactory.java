package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;
import ministere.sante.senpna.config.AppProperties;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Construit une paire (access + refresh) et encapsule le TTL configuré.
 * Le TTL en secondes est lu depuis {@code AppProperties} — {@link TokenPort}
 * ne l'expose pas (il n'a pas à connaître la configuration).
 */
@Component
public class AuthTokenFactory {

    private final TokenPort tokenPort;
    private final AppProperties appProperties;

    public AuthTokenFactory(TokenPort tokenPort, AppProperties appProperties) {
        this.tokenPort = tokenPort;
        this.appProperties = appProperties;
    }

    public AuthTokens build(User user, Set<String> roleCodes) {
        String accessToken = tokenPort.genererAccess(user, roleCodes);
        String refreshToken = tokenPort.genererRefresh(user);
        long expiresInSeconds = appProperties.jwt().accessTokenTtl().toSeconds();
        return new AuthTokens(accessToken, refreshToken, expiresInSeconds);
    }
}
