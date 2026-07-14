package ministere.sante.senpna.appeloffre.infrastructure.persistence.repository;

import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneAppelOffreJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LigneAppelOffreJpaRepository extends JpaRepository<LigneAppelOffreJpaEntity, UUID> {

    List<LigneAppelOffreJpaEntity> findByAppelOffreId(UUID appelOffreId);

    void deleteByAppelOffreId(UUID appelOffreId);
}
