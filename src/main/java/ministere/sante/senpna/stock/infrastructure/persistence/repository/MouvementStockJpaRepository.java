package ministere.sante.senpna.stock.infrastructure.persistence.repository;

import ministere.sante.senpna.stock.infrastructure.persistence.entity.MouvementStockJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MouvementStockJpaRepository extends JpaRepository<MouvementStockJpaEntity, UUID>,
        JpaSpecificationExecutor<MouvementStockJpaEntity> {
}
