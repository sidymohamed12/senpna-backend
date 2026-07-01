package ministere.sante.senpna.auth.application.usecase;

import ministere.sante.senpna.auth.application.service.UserDetailAssembler;
import ministere.sante.senpna.auth.domain.command.UserCommands.ListUsersQuery;
import ministere.sante.senpna.auth.domain.command.UserCommands.UserPage;
import ministere.sante.senpna.auth.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.port.in.ListUsersUseCase;
import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsersUseCaseImpl implements ListUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public ListUsersUseCaseImpl(UserRepositoryPort userRepositoryPort, UserDetailAssembler userDetailAssembler) {
        this.userRepositoryPort = userRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPage lister(ListUsersQuery query) {
        UserSearchCriteria criteria = new UserSearchCriteria(query.recherche(), query.actif(), query.roleId());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<User> result = userRepositoryPort.search(criteria, pageRequest);

        return new UserPage(
                result.content().stream().map(userDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
