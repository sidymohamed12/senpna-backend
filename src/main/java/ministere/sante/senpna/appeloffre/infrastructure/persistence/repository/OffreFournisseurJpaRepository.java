package ministere.sante.senpna.appeloffre.infrastructure.persistence.repository;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.OffreFournisseurJpaEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OffreFournisseurJpaRepository extends JpaRepository<OffreFournisseurJpaEntity, UUID> {

    boolean existsByAppelOffreIdAndFournisseurIdAndStatut(UUID appelOffreId, UUID fournisseurId, StatutOffre statut);

    Page<OffreFournisseurJpaEntity> findByAppelOffreId(UUID appelOffreId, Pageable pageable);

    Page<OffreFournisseurJpaEntity> findByFournisseurId(UUID fournisseurId, Pageable pageable);

    Page<OffreFournisseurJpaEntity> findByFournisseurIdAndStatut(UUID fournisseurId, StatutOffre statut,
            Pageable pageable);
}
