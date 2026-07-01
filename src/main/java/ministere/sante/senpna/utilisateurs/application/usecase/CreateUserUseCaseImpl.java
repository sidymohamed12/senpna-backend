package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.utilisateurs.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.utilisateurs.domain.exception.EmailDejaUtiliseException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.port.in.CreateUserUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleQueryPort roleQueryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final UserDetailAssembler userDetailAssembler;

    public CreateUserUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort, RoleQueryPort roleQueryPort,
            PasswordEncoderPort passwordEncoderPort, TemporaryPasswordGenerator temporaryPasswordGenerator,
            UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleQueryPort = roleQueryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public CreatedUser creer(CreateUserCommand command) {
        Email email = Email.of(command.email());

        if (userManagementRepositoryPort.existsByEmail(email)) {
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

        User saved = userManagementRepositoryPort.save(user);

        return new CreatedUser(userDetailAssembler.assembler(saved), motDePasseTemporaire);
    }
}
