package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.ResendOtpCommand;

public interface ResendOtpUseCase {
    void renvoyer(ResendOtpCommand command);
}
