package ministere.sante.senpna.organisation.infrastructure.persistence.repository;

import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StructureSanitaireJpaRepository extends JpaRepository<StructureSanitaireJpaEntity, UUID>,
        JpaSpecificationExecutor<StructureSanitaireJpaEntity> {

    boolean existsByCode(String code);
}
