package ministere.sante.senpna.carriere.infrastructure.persistence.repository;

import ministere.sante.senpna.carriere.infrastructure.persistence.entity.CandidatureJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CandidatureJpaRepository extends JpaRepository<CandidatureJpaEntity, UUID>,
                JpaSpecificationExecutor<CandidatureJpaEntity> {
}
