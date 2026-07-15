package ministere.sante.senpna.commandeachat.infrastructure.persistence.repository;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.FactureJpaEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FactureJpaRepository extends JpaRepository<FactureJpaEntity, UUID> {

    Page<FactureJpaEntity> findAll(Pageable pageable);

    Page<FactureJpaEntity> findByStatut(StatutFacture statut, Pageable pageable);

    Page<FactureJpaEntity> findByFournisseurId(UUID fournisseurId, Pageable pageable);

    Page<FactureJpaEntity> findByFournisseurIdAndStatut(UUID fournisseurId, StatutFacture statut, Pageable pageable);
}
