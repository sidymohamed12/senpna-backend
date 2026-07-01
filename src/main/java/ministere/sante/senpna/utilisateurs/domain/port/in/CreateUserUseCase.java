package ministere.sante.senpna.utilisateurs.domain.port.in;

import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;

public interface CreateUserUseCase {
    CreatedUser creer(CreateUserCommand command);
}
