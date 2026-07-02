package ministere.sante.senpna.fournisseur.infrastructure.persistence.repository;

import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FournisseurJpaRepository extends JpaRepository<FournisseurJpaEntity, UUID>,
        JpaSpecificationExecutor<FournisseurJpaEntity> {

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, UUID id);
}
