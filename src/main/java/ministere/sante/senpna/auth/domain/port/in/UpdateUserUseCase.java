package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.UpdateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;

public interface UpdateUserUseCase {
    UserDetail modifier(UpdateUserCommand command);
}
