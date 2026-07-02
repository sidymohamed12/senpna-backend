package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RevokeRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.exception.DernierRoleException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleNonAssigneException;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.utilisateurs.domain.port.in.RevokeRoleUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeRoleUseCaseImpl implements RevokeRoleUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final UserHierarchyGuard userHierarchyGuard;
    private final UserDetailAssembler userDetailAssembler;

    public RevokeRoleUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort,
            UserHierarchyGuard userHierarchyGuard, UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.userHierarchyGuard = userHierarchyGuard;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail retirer(RevokeRoleCommand command) {
        User user = userManagementRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        if (!user.getRoleIds().contains(command.roleId())) {
            throw new RoleNonAssigneException();
        }

        if (user.getRoleIds().size() == 1) {
            throw new DernierRoleException();
        }

        // Vérifié sur les rôles actuels (avant retrait) : un ADMIN_PRA ne
        // doit pas pouvoir toucher un compte national ou un autre
        // ADMIN_PRA, y compris pour lui retirer un rôle.
        userHierarchyGuard.verifierGestionAutorisee(command.acteurId(), user.getRoleIds());

        user.retirerRole(command.roleId());

        User saved = userManagementRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
