package ministere.sante.senpna.commandeachat.infrastructure.persistence.repository;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.CommandeAchatJpaEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CommandeAchatJpaRepository extends JpaRepository<CommandeAchatJpaEntity, UUID>,
        JpaSpecificationExecutor<CommandeAchatJpaEntity> {

    boolean existsByReferenceIgnoreCase(String reference);

    Page<CommandeAchatJpaEntity> findByFournisseurId(UUID fournisseurId, Pageable pageable);

    Page<CommandeAchatJpaEntity> findByFournisseurIdAndStatut(UUID fournisseurId, StatutCommandeAchat statut,
            Pageable pageable);
}
