package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ActivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

public interface ActivateUserUseCase {
    UserDetail activer(ActivateUserCommand command);
}
