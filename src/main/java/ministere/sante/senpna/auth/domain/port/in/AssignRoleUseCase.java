package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.AssignRoleCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;

public interface AssignRoleUseCase {
    UserDetail assigner(AssignRoleCommand command);
}
