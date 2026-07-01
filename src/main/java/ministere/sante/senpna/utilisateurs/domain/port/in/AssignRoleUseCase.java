package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.AssignRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

public interface AssignRoleUseCase {
    UserDetail assigner(AssignRoleCommand command);
}
