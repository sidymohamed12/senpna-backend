package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.ForgotPasswordCommand;

public interface ForgotPasswordUseCase {
    void demander(ForgotPasswordCommand command);
}
