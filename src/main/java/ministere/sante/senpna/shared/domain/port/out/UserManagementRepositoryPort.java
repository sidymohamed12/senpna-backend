package ministere.sante.senpna.shared.domain.port.out;

import java.util.Optional;

import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.utilisateurs.domain.criteria.UserSearchCriteria;

public interface UserManagementRepositoryPort {
    PageResult<User> search(UserSearchCriteria criteria, PageRequest pageRequest);

    Optional<User> findById(UserId id);

    User save(User user);

    boolean existsByEmail(Email email);

}
