package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.valueobject.Nom;
import ministere.sante.senpna.auth.domain.valueobject.Phone;
import ministere.sante.senpna.auth.domain.valueobject.Prenom;
import ministere.sante.senpna.auth.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UpdateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.exception.UserNotFoundException;
import ministere.sante.senpna.utilisateurs.domain.port.in.UpdateUserUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public UpdateUserUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort,
            UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail modifier(UpdateUserCommand command) {
        User user = userManagementRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        user.renommer(Nom.of(command.nom()), Prenom.of(command.prenom()));

        Phone telephone = (command.telephone() != null && !command.telephone().isBlank())
                ? Phone.of(command.telephone())
                : null;
        user.modifierTelephone(telephone);

        User saved = userManagementRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
