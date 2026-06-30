package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
