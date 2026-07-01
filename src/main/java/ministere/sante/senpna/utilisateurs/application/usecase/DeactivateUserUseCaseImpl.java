package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.DeactivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.exception.AutoDesactivationInterditeException;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.utilisateurs.domain.port.in.DeactivateUserUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateUserUseCaseImpl implements DeactivateUserUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public DeactivateUserUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort,
            UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail desactiver(DeactivateUserCommand command) {
        // Invariant de sécurité : un administrateur ne peut pas se
        // désactiver lui-même (évite un auto-verrouillage accidentel).
        if (command.userId().equals(command.acteurId())) {
            throw new AutoDesactivationInterditeException();
        }

        User user = userManagementRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        user.desactiver();

        User saved = userManagementRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
