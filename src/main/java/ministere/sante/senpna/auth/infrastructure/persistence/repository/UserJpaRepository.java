package ministere.sante.senpna.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ministere.sante.senpna.auth.infrastructure.persistence.entity.UserJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID>,
        JpaSpecificationExecutor<UserJpaEntity> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
