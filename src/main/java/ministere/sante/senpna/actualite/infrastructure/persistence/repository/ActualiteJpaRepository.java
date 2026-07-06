package ministere.sante.senpna.actualite.infrastructure.persistence.repository;

import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ActualiteJpaRepository extends JpaRepository<ActualiteJpaEntity, UUID>,
                JpaSpecificationExecutor<ActualiteJpaEntity> {
}
