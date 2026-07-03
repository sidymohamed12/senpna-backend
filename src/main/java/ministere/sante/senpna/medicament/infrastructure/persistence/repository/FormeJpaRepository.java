package ministere.sante.senpna.medicament.infrastructure.persistence.repository;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FormeJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FormeJpaRepository extends JpaRepository<FormeJpaEntity, UUID>,
        JpaSpecificationExecutor<FormeJpaEntity> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
