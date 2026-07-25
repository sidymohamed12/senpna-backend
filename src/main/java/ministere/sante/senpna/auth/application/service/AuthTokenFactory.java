package ministere.sante.senpna.auth.application.service;

import com.sidymohamed12.jwt.spring.autoconfigure.JwtProperties;

import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Construit une paire (access + refresh) et encapsule le TTL configuré.
 * Le TTL en secondes est lu depuis {@link JwtProperties} (jwt-toolkit) —
 * {@link TokenPort} ne l'expose pas (il n'a pas à connaître la
 * configuration).
 *
 * <p>
 * L'affectation organisationnelle de l'utilisateur ({@code entrepotId} /
 * {@code structureSanitaireId}), quand elle existe, est également
 * embarquée dans l'access token — utile côté client pour éviter un appel
 * supplémentaire.
 * </p>
 */
@Component
public class AuthTokenFactory {

    private final TokenPort tokenPort;
    private final JwtProperties jwtProperties;
    private final UserAffectationResolver userAffectationResolver;

    public AuthTokenFactory(TokenPort tokenPort, JwtProperties jwtProperties,
            UserAffectationResolver userAffectationResolver) {
        this.tokenPort = tokenPort;
        this.jwtProperties = jwtProperties;
        this.userAffectationResolver = userAffectationResolver;
    }

    public AuthTokens build(User user, Set<String> roleCodes) {
        UserAffectationView affectation = userAffectationResolver.resoudre(user.getId().getValue());

        String accessToken = tokenPort.genererAccess(user, roleCodes,
                affectation.entrepotId(), affectation.structureSanitaireId(), affectation.fournisseurId());
        String refreshToken = tokenPort.genererRefresh(user);
        long expiresInSeconds = jwtProperties.accessTokenTtl().toSeconds();
        return new AuthTokens(accessToken, refreshToken, expiresInSeconds);
    }
}
