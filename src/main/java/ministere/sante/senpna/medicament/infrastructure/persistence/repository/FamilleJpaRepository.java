package ministere.sante.senpna.medicament.infrastructure.persistence.repository;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FamilleJpaRepository extends JpaRepository<FamilleJpaEntity, UUID>,
        JpaSpecificationExecutor<FamilleJpaEntity> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
