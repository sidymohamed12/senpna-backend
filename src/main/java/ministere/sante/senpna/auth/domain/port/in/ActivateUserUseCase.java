package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.ActivateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;

public interface ActivateUserUseCase {
    UserDetail activer(ActivateUserCommand command);
}
