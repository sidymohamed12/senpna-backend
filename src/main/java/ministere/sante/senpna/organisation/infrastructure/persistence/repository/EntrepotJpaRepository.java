package ministere.sante.senpna.organisation.infrastructure.persistence.repository;

import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntrepotJpaRepository extends JpaRepository<EntrepotJpaEntity, UUID>,
        JpaSpecificationExecutor<EntrepotJpaEntity> {

    boolean existsByCode(String code);

    /**
     * Donnée de référence unique — cf.
     * {@code EntrepotQueryPort.findPnaCentraleActive}.
     */
    Optional<EntrepotJpaEntity> findFirstByTypeAndActifTrue(TypeEntrepot type);

    List<EntrepotJpaEntity> findByTypeAndActifTrueOrderByCodeAsc(TypeEntrepot type);

    List<EntrepotJpaEntity> findByTypeAndRegionIdAndActifTrueOrderByCodeAsc(TypeEntrepot type, UUID regionId);
}
