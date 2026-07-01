package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.OtpService;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.VerifyOtpUseCase;
import ministere.sante.senpna.auth.domain.port.out.ResetTokenPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valide le code OTP soumis et délivre un jeton de réinitialisation
 * court-lived (5 min), à usage unique, requis par {@link ResetPasswordUseCase}.
 *
 * <p>
 * Le jeton est désormais géré par {@link ResetTokenPort} (et non plus
 * inliné dans {@code OtpService}) : les responsabilités sont correctement
 * séparées — {@code OtpService} gère les codes OTP, {@code ResetTokenPort}
 * gère les jetons de réinitialisation.
 * </p>
 */
@Service
public class VerifyOtpUseCaseImpl implements VerifyOtpUseCase {

    private final OtpService otpService;
    private final ResetTokenPort resetTokenPort;
    private final UserRepositoryPort userRepositoryPort;

    public VerifyOtpUseCaseImpl(OtpService otpService, ResetTokenPort resetTokenPort,
            UserRepositoryPort userRepositoryPort) {
        this.otpService = otpService;
        this.resetTokenPort = resetTokenPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public VerifyOtpResult verifier(VerifyOtpCommand command) {
        // Valide le code OTP (lève une exception si invalide/expiré)
        otpService.valider(command.email(), command.code());

        // Résolution de l'utilisateur pour obtenir son UUID
        User user = userRepositoryPort.findByEmail(Email.of(command.email()))
                .orElseThrow(UserNotFoundException::new);

        // Génération du jeton de réinitialisation via le port dédié
        String resetToken = resetTokenPort.genererResetToken(
                user.getId().getValue(),
                user.getEmail().value());

        return new VerifyOtpResult(resetToken);
    }
}
