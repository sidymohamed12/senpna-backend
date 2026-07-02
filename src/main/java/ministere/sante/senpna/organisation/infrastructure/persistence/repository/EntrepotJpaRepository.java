package ministere.sante.senpna.organisation.infrastructure.persistence.repository;

import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface EntrepotJpaRepository extends JpaRepository<EntrepotJpaEntity, UUID>,
        JpaSpecificationExecutor<EntrepotJpaEntity> {

    boolean existsByCode(String code);
}
