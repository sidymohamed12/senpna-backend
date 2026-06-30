package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.port.in.ResetPasswordUseCase;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;
import ministere.sante.senpna.auth.application.service.OtpService;
import ministere.sante.senpna.auth.domain.exception.UserNotFoundException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordUseCaseImpl(UserRepositoryPort userRepositoryPort, OtpService otpService,
            PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void reinitialiser(ResetPasswordCommand command) {
        String email = otpService.consommerJetonReinitialisation(command.resetToken());

        User user = userRepositoryPort.findByEmail(Email.of(email))
                .orElseThrow(UserNotFoundException::new);

        String hash = passwordEncoder.encode(command.nouveauMotDePasse());
        user.changerMotDePasse(HashedPassword.of(hash));
        userRepositoryPort.save(user);
    }
}
