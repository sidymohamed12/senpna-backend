package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.DeactivateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.auth.domain.exception.AutoDesactivationInterditeException;
import ministere.sante.senpna.auth.domain.exception.UserNotFoundException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.DeactivateUserUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateUserUseCaseImpl implements DeactivateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public DeactivateUserUseCaseImpl(UserRepositoryPort userRepositoryPort,
            UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail desactiver(DeactivateUserCommand command) {
        // Invariant de sécurité : un administrateur ne peut pas se
        // désactiver lui-même (évite un auto-verrouillage accidentel).
        if (command.userId().equals(command.acteurId())) {
            throw new AutoDesactivationInterditeException();
        }

        User user = userRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        user.desactiver();

        User saved = userRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
