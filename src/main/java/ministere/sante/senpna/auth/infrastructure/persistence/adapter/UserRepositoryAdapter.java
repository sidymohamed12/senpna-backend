package ministere.sante.senpna.auth.infrastructure.persistence.adapter;

import ministere.sante.senpna.auth.domain.port.out.UserRepositoryPort;
import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;
import ministere.sante.senpna.auth.infrastructure.persistence.mapper.UserMapper;
import ministere.sante.senpna.auth.infrastructure.persistence.repository.UserJpaRepository;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.value()).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.getValue()).map(userMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity saved = userJpaRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }
}
