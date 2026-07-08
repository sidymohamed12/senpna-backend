package ministere.sante.senpna.carriere.infrastructure.persistence.repository;

import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.OpportuniteCarriereJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OpportuniteCarriereJpaRepository extends JpaRepository<OpportuniteCarriereJpaEntity, UUID>,
                JpaSpecificationExecutor<OpportuniteCarriereJpaEntity> {

        List<OpportuniteCarriereJpaEntity> findByStatutInAndDateLimiteCandidatureBefore(
                        List<StatutOpportunite> statuts, LocalDate reference);
}
