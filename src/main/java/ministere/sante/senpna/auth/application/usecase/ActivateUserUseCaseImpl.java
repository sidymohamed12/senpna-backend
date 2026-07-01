package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.ActivateUserCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.auth.domain.exception.UserNotFoundException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.ActivateUserUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateUserUseCaseImpl implements ActivateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public ActivateUserUseCaseImpl(UserRepositoryPort userRepositoryPort, UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail activer(ActivateUserCommand command) {
        User user = userRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        user.activer();
        // Un compte réactivé repart sur des bases saines : plus d'échecs
        // de connexion historiques, plus de verrouillage temporaire.
        user.reinitialiserEchecsConnexion();

        User saved = userRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
