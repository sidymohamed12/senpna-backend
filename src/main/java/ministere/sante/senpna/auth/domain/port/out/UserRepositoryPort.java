package ministere.sante.senpna.auth.domain.port.out;

import ministere.sante.senpna.auth.domain.model.User;
import ministere.sante.senpna.auth.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.valueobject.Email;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByEmail(Email email);

    Optional<User> findById(UserId id);

    User save(User user);

    boolean existsByEmail(Email email);
}
