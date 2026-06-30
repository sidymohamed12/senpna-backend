package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.port.in.VerifyOtpUseCase;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.auth.application.service.OtpService;

import org.springframework.stereotype.Service;

@Service
public class VerifyOtpUseCaseImpl implements VerifyOtpUseCase {

    private final OtpService otpService;

    public VerifyOtpUseCaseImpl(OtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    public VerifyOtpResult verifier(VerifyOtpCommand command) {
        String resetToken = otpService.valider(command.email(), command.code());
        return new VerifyOtpResult(resetToken);
    }
}
