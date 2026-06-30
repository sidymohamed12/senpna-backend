package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void reinitialiser(ResetPasswordCommand command);
}
