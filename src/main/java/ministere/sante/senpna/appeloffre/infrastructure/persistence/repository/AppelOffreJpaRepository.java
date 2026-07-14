package ministere.sante.senpna.appeloffre.infrastructure.persistence.repository;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.AppelOffreJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppelOffreJpaRepository extends JpaRepository<AppelOffreJpaEntity, UUID>,
        JpaSpecificationExecutor<AppelOffreJpaEntity> {

    boolean existsByReferenceIgnoreCase(String reference);

    List<AppelOffreJpaEntity> findByStatutAndDateClotureLessThan(StatutAppelOffre statut, LocalDate date);
}
