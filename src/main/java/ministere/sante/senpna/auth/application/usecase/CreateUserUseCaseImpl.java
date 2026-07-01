package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.auth.domain.exception.EmailDejaUtiliseException;
import ministere.sante.senpna.auth.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.CreateUserUseCase;
import ministere.sante.senpna.auth.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.HashedPassword;
import ministere.sante.senpna.auth.domain.valueobject.Nom;
import ministere.sante.senpna.auth.domain.valueobject.Phone;
import ministere.sante.senpna.auth.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.infrastructure.exception.ValidationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleQueryPort roleQueryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final UserDetailAssembler userDetailAssembler;

    public CreateUserUseCaseImpl(UserRepositoryPort userRepositoryPort, RoleQueryPort roleQueryPort,
            PasswordEncoderPort passwordEncoderPort, TemporaryPasswordGenerator temporaryPasswordGenerator,
            UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleQueryPort = roleQueryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public CreatedUser creer(CreateUserCommand command) {
        Email email = Email.of(command.email());

        if (userRepositoryPort.existsByEmail(email)) {
            throw new EmailDejaUtiliseException(email.value());
        }

        Set<UUID> roleIds = command.roleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ValidationException("Un utilisateur doit posséder au moins un rôle", "ROLE_REQUIRED");
        }
        for (UUID roleId : roleIds) {
            if (!roleQueryPort.existsById(roleId)) {
                throw new RoleIntrouvableException();
            }
        }

        Phone telephone = (command.telephone() != null && !command.telephone().isBlank())
                ? Phone.of(command.telephone())
                : null;

        String motDePasseTemporaire = temporaryPasswordGenerator.generer();
        HashedPassword hashedPassword = HashedPassword.of(passwordEncoderPort.encoder(motDePasseTemporaire));

        User user = User.creer(
                Nom.of(command.nom()),
                Prenom.of(command.prenom()),
                email,
                telephone,
                hashedPassword,
                roleIds);

        User saved = userRepositoryPort.save(user);

        return new CreatedUser(userDetailAssembler.assembler(saved), motDePasseTemporaire);
    }
}
