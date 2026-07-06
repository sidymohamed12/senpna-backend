package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.LogoutCommand;

public interface LogoutUseCase {
    void logout(LogoutCommand command);
}
