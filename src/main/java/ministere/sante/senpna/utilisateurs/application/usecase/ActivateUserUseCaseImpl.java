package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ActivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.utilisateurs.domain.port.in.ActivateUserUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateUserUseCaseImpl implements ActivateUserUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final UserHierarchyGuard userHierarchyGuard;
    private final UserDetailAssembler userDetailAssembler;

    public ActivateUserUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort,
            UserHierarchyGuard userHierarchyGuard, UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.userHierarchyGuard = userHierarchyGuard;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail activer(ActivateUserCommand command) {
        User user = userManagementRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        userHierarchyGuard.verifierGestionAutorisee(command.acteurId(), user.getRoleIds());

        user.activer();
        // Un compte réactivé repart sur des bases saines : plus d'échecs
        // de connexion historiques, plus de verrouillage temporaire.
        user.reinitialiserEchecsConnexion();

        User saved = userManagementRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
