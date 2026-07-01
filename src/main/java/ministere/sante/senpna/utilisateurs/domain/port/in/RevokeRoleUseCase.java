package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RevokeRoleCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

public interface RevokeRoleUseCase {
    UserDetail retirer(RevokeRoleCommand command);
}
