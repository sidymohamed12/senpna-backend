package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UpdateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

public interface UpdateUserUseCase {
    UserDetail modifier(UpdateUserCommand command);
}
