package ministere.sante.senpna.medicament.infrastructure.persistence.repository;

import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ConditionnementJpaRepository extends JpaRepository<ConditionnementJpaEntity, UUID>,
        JpaSpecificationExecutor<ConditionnementJpaEntity> {

    boolean existsByMedicamentIdAndNiveau(UUID medicamentId, int niveau);

    boolean existsByMedicamentIdAndNiveauAndIdNot(UUID medicamentId, int niveau, UUID id);

    boolean existsByMedicamentIdAndNomIgnoreCase(UUID medicamentId, String nom);

    boolean existsByMedicamentIdAndNomIgnoreCaseAndIdNot(UUID medicamentId, String nom, UUID id);

    boolean existsByMedicamentIdAndEstUniteBaseTrue(UUID medicamentId);

    boolean existsByMedicamentIdAndEstUniteBaseTrueAndIdNot(UUID medicamentId, UUID id);

    long countByMedicamentIdAndEstUniteBaseTrueAndActifTrue(UUID medicamentId);
}
