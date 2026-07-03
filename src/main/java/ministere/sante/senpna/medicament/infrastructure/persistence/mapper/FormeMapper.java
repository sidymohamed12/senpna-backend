package ministere.sante.senpna.medicament.infrastructure.persistence.mapper;

import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.infrastructure.persistence.entity.FormeJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class FormeMapper {

    public Forme toDomain(FormeJpaEntity entity) {
        return Forme.reconstruct(
                FormeId.of(entity.getId()),
                entity.getCode(),
                entity.getLibelle(),
                entity.getDescription(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public FormeJpaEntity toEntity(Forme forme) {
        FormeJpaEntity entity = new FormeJpaEntity(
                forme.getId().getValue(),
                forme.getCode(),
                forme.getLibelle(),
                forme.getDescription(),
                forme.isActif());
        entity.setCreatedAt(forme.getCreatedAt());
        entity.setUpdatedAt(forme.getUpdatedAt());
        return entity;
    }
}
