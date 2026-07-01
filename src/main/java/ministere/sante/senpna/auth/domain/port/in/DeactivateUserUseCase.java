package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.DeactivateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;

public interface DeactivateUserUseCase {
    UserDetail desactiver(DeactivateUserCommand command);
}
