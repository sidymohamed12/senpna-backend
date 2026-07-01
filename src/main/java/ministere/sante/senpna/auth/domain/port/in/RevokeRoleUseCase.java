package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.RevokeRoleCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;

public interface RevokeRoleUseCase {
    UserDetail retirer(RevokeRoleCommand command);
}
