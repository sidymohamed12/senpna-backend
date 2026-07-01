package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.UpdateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.auth.domain.exception.UserNotFoundException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.UpdateUserUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.Nom;
import ministere.sante.senpna.auth.domain.valueobject.Phone;
import ministere.sante.senpna.auth.domain.valueobject.Prenom;
import ministere.sante.senpna.auth.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public UpdateUserUseCaseImpl(UserRepositoryPort userRepositoryPort, UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail modifier(UpdateUserCommand command) {
        User user = userRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        user.renommer(Nom.of(command.nom()), Prenom.of(command.prenom()));

        Phone telephone = (command.telephone() != null && !command.telephone().isBlank())
                ? Phone.of(command.telephone())
                : null;
        user.modifierTelephone(telephone);

        User saved = userRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
