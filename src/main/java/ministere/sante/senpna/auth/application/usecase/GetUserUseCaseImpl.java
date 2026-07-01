package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.GetUserQuery;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.auth.domain.exception.UserNotFoundException;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.GetUserUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetUserUseCaseImpl implements GetUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public GetUserUseCaseImpl(UserRepositoryPort userRepositoryPort, UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetail obtenir(GetUserQuery query) {
        User user = userRepositoryPort.findById(UserId.of(query.userId()))
                .orElseThrow(UserNotFoundException::new);
        return userDetailAssembler.assembler(user);
    }
}
