package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.AuthTokenFactory;
import ministere.sante.senpna.auth.application.service.UserRoleResolver;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.auth.domain.exception.CompteInactifException;
import ministere.sante.senpna.auth.domain.exception.CompteVerrouilleException;
import ministere.sante.senpna.auth.domain.exception.InvalidCredentialsException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.LoginUseCase;
import ministere.sante.senpna.auth.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private static final int SEUIL_VERROUILLAGE = 5;
    private static final long DUREE_VERROUILLAGE_MINUTES = 15;

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final AuthTokenFactory authTokenFactory;
    private final UserRoleResolver userRoleResolver;

    public LoginUseCaseImpl(UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort,
            AuthTokenFactory authTokenFactory, UserRoleResolver userRoleResolver) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.authTokenFactory = authTokenFactory;
        this.userRoleResolver = userRoleResolver;
    }

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        Email email = Email.of(command.email());

        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.estVerrouille()) {
            throw new CompteVerrouilleException();
        }

        if (!passwordEncoderPort.correspond(command.password(), user.getHashedPassword().value())) {
            user.enregistrerEchecConnexion(SEUIL_VERROUILLAGE,
                    Instant.now().plus(DUREE_VERROUILLAGE_MINUTES, ChronoUnit.MINUTES));
            userRepositoryPort.save(user);
            throw new InvalidCredentialsException();
        }

        if (!user.isActif()) {
            throw new CompteInactifException();
        }

        user.reinitialiserEchecsConnexion();
        userRepositoryPort.save(user);

        Set<String> roleCodes = userRoleResolver.resoudreCodes(user.getRoleIds());
        AuthTokens tokens = authTokenFactory.build(user, roleCodes);

        return new LoginResult(tokens, new UserSummary(
                user.getId().getValue(),
                user.getNom().getValue(),
                user.getPrenom().getValue(),
                user.getEmail().value(),
                roleCodes));
    }

}
