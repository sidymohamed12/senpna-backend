package ministere.sante.senpna.utilisateurs.application.facade;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ActivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.AssignRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.DeactivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.GetUserQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ListUsersQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RevokeRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UpdateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserPage;
import ministere.sante.senpna.utilisateurs.domain.port.in.ActivateUserUseCase;
import ministere.sante.senpna.utilisateurs.domain.port.in.AssignRoleUseCase;
import ministere.sante.senpna.utilisateurs.domain.port.in.CreateUserUseCase;
import ministere.sante.senpna.utilisateurs.domain.port.in.DeactivateUserUseCase;
import ministere.sante.senpna.utilisateurs.domain.port.in.GetUserUseCase;
import ministere.sante.senpna.utilisateurs.domain.port.in.ListUsersUseCase;
import ministere.sante.senpna.utilisateurs.domain.port.in.RevokeRoleUseCase;
import ministere.sante.senpna.utilisateurs.domain.port.in.UpdateUserUseCase;

import org.springframework.stereotype.Component;

@Component
public class UserManagementFacade {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final RevokeRoleUseCase revokeRoleUseCase;

    public UserManagementFacade(
            CreateUserUseCase createUserUseCase,
            GetUserUseCase getUserUseCase,
            ListUsersUseCase listUsersUseCase,
            UpdateUserUseCase updateUserUseCase,
            ActivateUserUseCase activateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase,
            AssignRoleUseCase assignRoleUseCase,
            RevokeRoleUseCase revokeRoleUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.assignRoleUseCase = assignRoleUseCase;
        this.revokeRoleUseCase = revokeRoleUseCase;
    }

    public CreatedUser creer(CreateUserCommand command) {
        return createUserUseCase.creer(command);
    }

    public UserDetail obtenir(GetUserQuery query) {
        return getUserUseCase.obtenir(query);
    }

    public UserPage lister(ListUsersQuery query) {
        return listUsersUseCase.lister(query);
    }

    public UserDetail modifier(UpdateUserCommand command) {
        return updateUserUseCase.modifier(command);
    }

    public UserDetail activer(ActivateUserCommand command) {
        return activateUserUseCase.activer(command);
    }

    public UserDetail desactiver(DeactivateUserCommand command) {
        return deactivateUserUseCase.desactiver(command);
    }

    public UserDetail assignerRole(AssignRoleCommand command) {
        return assignRoleUseCase.assigner(command);
    }

    public UserDetail retirerRole(RevokeRoleCommand command) {
        return revokeRoleUseCase.retirer(command);
    }
}
