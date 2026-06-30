package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.config.JwtService;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class AuthTokenFactory {

    private final JwtService jwtService;
    private final AppProperties appProperties;

    public AuthTokenFactory(JwtService jwtService, AppProperties appProperties) {
        this.jwtService = jwtService;
        this.appProperties = appProperties;
    }

    public AuthTokens build(String email, Set<String> roleCodes) {
        String accessToken = jwtService.generateAccessToken(email, Map.of("roles", roleCodes));
        String refreshToken = jwtService.generateRefreshToken(email);
        long expiresInSeconds = appProperties.jwt().accessTokenTtl().toSeconds();
        return new AuthTokens(accessToken, refreshToken, expiresInSeconds);
    }
}
