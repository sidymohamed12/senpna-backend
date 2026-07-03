package ministere.sante.senpna.medicament.infrastructure.persistence.repository;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.MedicamentJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MedicamentJpaRepository extends JpaRepository<MedicamentJpaEntity, UUID>,
        JpaSpecificationExecutor<MedicamentJpaEntity> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}
