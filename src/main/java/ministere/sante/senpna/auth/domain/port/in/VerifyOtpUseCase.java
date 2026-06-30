package ministere.sante.senpna.auth.domain.port.in;

import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;

public interface VerifyOtpUseCase {
    VerifyOtpResult verifier(VerifyOtpCommand command);
}
