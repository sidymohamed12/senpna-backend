package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.ResetPasswordUseCase;
import ministere.sante.senpna.auth.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.auth.domain.port.out.ResetTokenPort;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.HashedPassword;
import ministere.sante.senpna.auth.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.domain.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Finalise le changement de mot de passe.
 *
 * <p>
 * Le jeton de réinitialisation est consommé (usage unique) via
 * {@link ResetTokenPort} — ce qui fournit directement l'UUID de
 * l'utilisateur, évitant une requête DB supplémentaire par email.
 * </p>
 */
@Service
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ResetTokenPort resetTokenPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public ResetPasswordUseCaseImpl(UserRepositoryPort userRepositoryPort, ResetTokenPort resetTokenPort,
            PasswordEncoderPort passwordEncoderPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.resetTokenPort = resetTokenPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    @Transactional
    public void reinitialiser(ResetPasswordCommand command) {
        // Consomme le jeton (usage unique) → renvoie l'UUID de l'utilisateur
        UUID userId = resetTokenPort.validerEtExtraireUserId(command.resetToken());

        User user = userRepositoryPort.findById(UserId.of(userId))
                .orElseThrow(UserNotFoundException::new);

        String hash = passwordEncoderPort.encoder(command.nouveauMotDePasse());
        user.changerMotDePasse(HashedPassword.of(hash));
        userRepositoryPort.save(user);
    }
}
