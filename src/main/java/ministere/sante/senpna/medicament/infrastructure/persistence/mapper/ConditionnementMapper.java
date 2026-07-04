package ministere.sante.senpna.medicament.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.ConditionnementJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class ConditionnementMapper {

    public Conditionnement toDomain(ConditionnementJpaEntity entity) {
        return Conditionnement.reconstruct(
                ConditionnementId.of(entity.getId()),
                MedicamentId.of(entity.getMedicamentId()),
                entity.getNom(),
                entity.getNiveau(),
                entity.getQuantiteUniteBase(),
                entity.isEstUniteBase(),
                entity.getPrixAchat(),
                entity.getPrixVente(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public ConditionnementJpaEntity toEntity(Conditionnement conditionnement) {
        ConditionnementJpaEntity entity = new ConditionnementJpaEntity(
                conditionnement.getId().getValue(),
                conditionnement.getMedicamentId().getValue(),
                conditionnement.getNom(),
                conditionnement.getNiveau(),
                conditionnement.getQuantiteUniteBase(),
                conditionnement.isEstUniteBase(),
                conditionnement.getPrixAchat(),
                conditionnement.getPrixVente(),
                conditionnement.isActif());
        entity.setCreatedAt(conditionnement.getCreatedAt());
        entity.setUpdatedAt(conditionnement.getUpdatedAt());
        return entity;
    }
}
