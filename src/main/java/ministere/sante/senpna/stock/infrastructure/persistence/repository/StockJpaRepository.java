package ministere.sante.senpna.stock.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;

import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StockJpaRepository extends JpaRepository<StockJpaEntity, UUID>,
        JpaSpecificationExecutor<StockJpaEntity> {

    Optional<StockJpaEntity> findByEntrepotIdAndLotId(UUID entrepotId, UUID lotId);

    /**
     * Verrouillage pessimiste (SELECT ... FOR UPDATE) — protège les
     * quantités contre les mises à jour concurrentes lors des réservations
     * et mouvements simultanés sur une même ligne de stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockJpaEntity s where s.entrepotId = :entrepotId and s.lotId = :lotId")
    Optional<StockJpaEntity> findByEntrepotIdAndLotIdForUpdate(UUID entrepotId, UUID lotId);
}
