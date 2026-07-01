package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.GetUserQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.utilisateurs.domain.port.in.GetUserUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUserUseCaseImpl implements GetUserUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public GetUserUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort,
            UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetail obtenir(GetUserQuery query) {
        User user = userManagementRepositoryPort.findById(UserId.of(query.userId()))
                .orElseThrow(UserNotFoundException::new);
        return userDetailAssembler.assembler(user);
    }
}
