package ministere.sante.senpna.appeloffre.infrastructure.persistence.repository;

import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneOffreJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LigneOffreJpaRepository extends JpaRepository<LigneOffreJpaEntity, UUID> {

    List<LigneOffreJpaEntity> findByOffreId(UUID offreId);

    void deleteByOffreId(UUID offreId);
}
