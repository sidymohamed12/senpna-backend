package ministere.sante.senpna.stock.infrastructure.persistence.repository;

import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LotJpaRepository extends JpaRepository<LotJpaEntity, UUID>,
        JpaSpecificationExecutor<LotJpaEntity> {

    boolean existsByMedicamentIdAndNumeroLotIgnoreCase(UUID medicamentId, String numeroLot);

    List<LotJpaEntity> findByMedicamentIdAndStatutOrderByDateExpirationAsc(UUID medicamentId, String statut);

    List<LotJpaEntity> findByStatutAndDateExpirationBefore(String statut, LocalDate date);
}
