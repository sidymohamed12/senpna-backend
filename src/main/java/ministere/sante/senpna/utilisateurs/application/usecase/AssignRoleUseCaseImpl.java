package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.AssignRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleDejaAssigneException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.utilisateurs.domain.port.in.AssignRoleUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AssignRoleUseCaseImpl implements AssignRoleUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleQueryPort roleQueryPort;
    private final UserHierarchyGuard userHierarchyGuard;
    private final UserDetailAssembler userDetailAssembler;

    public AssignRoleUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort, RoleQueryPort roleQueryPort,
            UserHierarchyGuard userHierarchyGuard, UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleQueryPort = roleQueryPort;
        this.userHierarchyGuard = userHierarchyGuard;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail assigner(AssignRoleCommand command) {
        if (!roleQueryPort.existsById(command.roleId())) {
            throw new RoleIntrouvableException();
        }

        User user = userManagementRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        if (user.getRoleIds().contains(command.roleId())) {
            throw new RoleDejaAssigneException();
        }

        // Vérifie l'ensemble de rôles résultant (existants + nouveau) :
        // empêche un ADMIN_PRA d'élever un compte au rang national, ou
        // d'accorder le rôle ADMIN_PRA à un autre compte.
        Set<UUID> roleIdsApres = new HashSet<>(user.getRoleIds());
        roleIdsApres.add(command.roleId());
        userHierarchyGuard.verifierGestionAutorisee(command.acteurId(), roleIdsApres);

        user.ajouterRole(command.roleId());

        User saved = userManagementRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
