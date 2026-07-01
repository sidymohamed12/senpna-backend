package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.port.in.ResendOtpUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResendOtpCommand;
import ministere.sante.senpna.auth.application.service.OtpDestinationResolver;
import ministere.sante.senpna.auth.application.service.OtpService;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResendOtpUseCaseImpl implements ResendOtpUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final OtpService otpService;
    private final OtpDestinationResolver otpDestinationResolver;

    public ResendOtpUseCaseImpl(UserRepositoryPort userRepositoryPort, OtpService otpService,
            OtpDestinationResolver otpDestinationResolver) {
        this.userRepositoryPort = userRepositoryPort;
        this.otpService = otpService;
        this.otpDestinationResolver = otpDestinationResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public void renvoyer(ResendOtpCommand command) {
        Email email = Email.of(command.email());
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        String destination = otpDestinationResolver.resoudre(user, command.channel());
        otpService.genererEtEnvoyer(email.value(), command.channel(), destination);
    }
}
