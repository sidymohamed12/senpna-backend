package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.port.in.RefreshTokenUseCase;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;
import ministere.sante.senpna.auth.application.service.AuthTokenFactory;
import ministere.sante.senpna.auth.domain.exception.CompteInactifException;
import ministere.sante.senpna.auth.domain.exception.InvalidRefreshTokenException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.config.JwtService;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final JwtService jwtService;
    private final UserRepositoryPort userRepositoryPort;
    private final AuthTokenFactory authTokenFactory;
    private final UserRoleResolver userRoleResolver;

    public RefreshTokenUseCaseImpl(JwtService jwtService, UserRepositoryPort userRepositoryPort,
            AuthTokenFactory authTokenFactory, UserRoleResolver userRoleResolver) {
        this.jwtService = jwtService;
        this.userRepositoryPort = userRepositoryPort;
        this.authTokenFactory = authTokenFactory;
        this.userRoleResolver = userRoleResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokens rafraichir(RefreshTokenCommand command) {
        String token = command.refreshToken();

        try {
            if (!jwtService.isRefreshToken(token)) {
                throw new InvalidRefreshTokenException();
            }
            String email = jwtService.extractUsername(token);

            if (jwtService.extractExpiration(token).before(new java.util.Date())) {
                throw new InvalidRefreshTokenException();
            }

            User user = userRepositoryPort.findByEmail(Email.of(email))
                    .orElseThrow(InvalidRefreshTokenException::new);

            if (!user.isActif()) {
                throw new CompteInactifException();
            }

            Set<String> roleCodes = userRoleResolver.resoudreCodes(user.getRoleIds());
            return authTokenFactory.build(email, roleCodes);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }
    }

}
