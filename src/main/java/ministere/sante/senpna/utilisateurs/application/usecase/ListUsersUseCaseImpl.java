package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.ListUsersQuery;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserPage;
import ministere.sante.senpna.utilisateurs.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.utilisateurs.domain.port.in.ListUsersUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsersUseCaseImpl implements ListUsersUseCase {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public ListUsersUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort,
            UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPage lister(ListUsersQuery query) {
        UserSearchCriteria criteria = new UserSearchCriteria(query.recherche(), query.actif(), query.roleId());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<User> result = userManagementRepositoryPort.search(criteria, pageRequest);

        return new UserPage(
                result.content().stream().map(userDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
