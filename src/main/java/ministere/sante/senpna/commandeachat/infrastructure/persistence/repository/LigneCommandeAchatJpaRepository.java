package ministere.sante.senpna.commandeachat.infrastructure.persistence.repository;

import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.LigneCommandeAchatJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LigneCommandeAchatJpaRepository extends JpaRepository<LigneCommandeAchatJpaEntity, UUID> {

    List<LigneCommandeAchatJpaEntity> findByCommandeAchatId(UUID commandeAchatId);

    void deleteByCommandeAchatId(UUID commandeAchatId);
}
