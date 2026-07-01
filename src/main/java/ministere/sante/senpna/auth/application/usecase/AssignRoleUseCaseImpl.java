package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.AssignRoleCommand;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.auth.domain.exception.RoleDejaAssigneException;
import ministere.sante.senpna.auth.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.auth.domain.exception.UserNotFoundException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.AssignRoleUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignRoleUseCaseImpl implements AssignRoleUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleQueryPort roleQueryPort;
    private final UserDetailAssembler userDetailAssembler;

    public AssignRoleUseCaseImpl(UserRepositoryPort userRepositoryPort, RoleQueryPort roleQueryPort,
            UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleQueryPort = roleQueryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public UserDetail assigner(AssignRoleCommand command) {
        if (!roleQueryPort.existsById(command.roleId())) {
            throw new RoleIntrouvableException();
        }

        User user = userRepositoryPort.findById(UserId.of(command.userId()))
                .orElseThrow(UserNotFoundException::new);

        if (user.getRoleIds().contains(command.roleId())) {
            throw new RoleDejaAssigneException();
        }

        user.ajouterRole(command.roleId());

        User saved = userRepositoryPort.save(user);
        return userDetailAssembler.assembler(saved);
    }
}
