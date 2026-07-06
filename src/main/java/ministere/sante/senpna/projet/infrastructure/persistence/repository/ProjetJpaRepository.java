package ministere.sante.senpna.projet.infrastructure.persistence.repository;

import ministere.sante.senpna.projet.infrastructure.persistence.entity.ProjetJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ProjetJpaRepository extends JpaRepository<ProjetJpaEntity, UUID>,
                JpaSpecificationExecutor<ProjetJpaEntity> {
}
