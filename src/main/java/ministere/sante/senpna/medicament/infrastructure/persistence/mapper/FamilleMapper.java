package ministere.sante.senpna.medicament.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FamilleJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class FamilleMapper {

    public Famille toDomain(FamilleJpaEntity entity) {
        return Famille.reconstruct(
                FamilleId.of(entity.getId()),
                entity.getCode(),
                entity.getLibelle(),
                entity.getDescription(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public FamilleJpaEntity toEntity(Famille famille) {
        FamilleJpaEntity entity = new FamilleJpaEntity(
                famille.getId().getValue(),
                famille.getCode(),
                famille.getLibelle(),
                famille.getDescription(),
                famille.isActif());
        entity.setCreatedAt(famille.getCreatedAt());
        entity.setUpdatedAt(famille.getUpdatedAt());
        return entity;
    }
}
