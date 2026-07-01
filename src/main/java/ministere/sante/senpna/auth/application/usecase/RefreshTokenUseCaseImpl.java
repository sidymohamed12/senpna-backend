package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.AuthTokenFactory;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;
import ministere.sante.senpna.auth.domain.exception.CompteInactifException;
import ministere.sante.senpna.auth.domain.exception.InvalidRefreshTokenException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.RefreshTokenUseCase;
import ministere.sante.senpna.auth.domain.port.out.TokenPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Échange un refresh token valide contre une nouvelle paire de tokens.
 *
 * <h3>Révocation du refresh token consommé</h3>
 * <p>
 * À chaque rotation, l'ancien refresh token est révoqué immédiatement
 * (ajouté à la liste noire Redis). Cela implémente le pattern
 * <em>refresh token rotation</em> : si un attaquant intercepte un refresh
 * token et l'utilise avant le client légitime, le second usage (par le
 * client légitime) échouera car le token est révoqué — et vice versa.
 * </p>
 *
 * <h3>Vérification révocation du refresh token entrant</h3>
 * <p>
 * Avant toute validation métier, on vérifie que le refresh token n'est
 * pas déjà dans la liste noire (révoqué explicitement par un logout
 * ou une rotation précédente).
 * </p>
 */
@Service
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final TokenPort tokenPort;
    private final UserRepositoryPort userRepositoryPort;
    private final AuthTokenFactory authTokenFactory;
    private final UserRoleResolver userRoleResolver;

    public RefreshTokenUseCaseImpl(TokenPort tokenPort, UserRepositoryPort userRepositoryPort,
            AuthTokenFactory authTokenFactory, UserRoleResolver userRoleResolver) {
        this.tokenPort = tokenPort;
        this.userRepositoryPort = userRepositoryPort;
        this.authTokenFactory = authTokenFactory;
        this.userRoleResolver = userRoleResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokens rafraichir(RefreshTokenCommand command) {
        String oldRefreshToken = command.refreshToken();

        try {
            // ── Vérification révocation ──────────────────────────────────
            if (tokenPort.estInvalide(oldRefreshToken)) {
                throw new InvalidRefreshTokenException();
            }

            // ── Validation type + expiration ─────────────────────────────
            if (!tokenPort.estRefreshToken(oldRefreshToken)) {
                throw new InvalidRefreshTokenException();
            }
            if (tokenPort.estExpire(oldRefreshToken)) {
                throw new InvalidRefreshTokenException();
            }

            String email = tokenPort.extraireEmail(oldRefreshToken);

            User user = userRepositoryPort.findByEmail(Email.of(email))
                    .orElseThrow(InvalidRefreshTokenException::new);

            if (!user.isActif()) {
                throw new CompteInactifException();
            }

            // ── Rotation : révocation de l'ancien refresh token ──────────
            tokenPort.invalider(oldRefreshToken);

            // ── Émission des nouveaux tokens ─────────────────────────────
            Set<String> roleCodes = userRoleResolver.resoudreCodes(user.getRoleIds());
            return authTokenFactory.build(user, roleCodes);

        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }
    }
}
