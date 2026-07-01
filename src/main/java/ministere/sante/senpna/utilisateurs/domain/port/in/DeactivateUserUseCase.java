package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.DeactivateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

public interface DeactivateUserUseCase {
    UserDetail desactiver(DeactivateUserCommand command);
}
