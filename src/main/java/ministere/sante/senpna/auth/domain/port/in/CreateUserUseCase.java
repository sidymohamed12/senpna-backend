package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.CreatedUser;

public interface CreateUserUseCase {
    CreatedUser creer(CreateUserCommand command);
}
