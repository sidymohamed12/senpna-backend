package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.RevokeRoleCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.auth.domain.exception.DernierRoleException;
import ministere.sante.senpna.auth.domain.exception.RoleNonAssigneException;
import ministere.sante.senpna.auth.domain.exception.UserNotFoundException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.RevokeRoleUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeRoleUseCaseImpl implements RevokeRoleUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public RevokeRoleUseCaseImpl(UserRepositoryPort userRepositoryPort, UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail retirer(RevokeRoleCommand command) {
        User user = userRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        if (!user.getRoleIds().contains(command.roleId())) {
            throw new RoleNonAssigneException();
        }

        if (user.getRoleIds().size() == 1) {
            throw new DernierRoleException();
        }

        user.retirerRole(command.roleId());

        User saved = userRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
