package ministere.sante.senpna.organisation.infrastructure.persistence.mapper;

import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class EntrepotMapper {

    public Entrepot toDomain(EntrepotJpaEntity entity) {
        return Entrepot.builder()
            .id(EntrepotId.of(entity.getId()))
            .code(entity.getCode())
            .nom(entity.getNom())
            .type(entity.getType())
            .regionId(entity.getRegionId() != null ? RegionId.of(entity.getRegionId()) : null)
            .adresse(entity.getAdresse())
            .telephone(entity.getTelephone())
            .responsableUserId(entity.getResponsableUserId())
            .actif(entity.isActif())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public EntrepotJpaEntity toEntity(Entrepot entrepot) {
        EntrepotJpaEntity entity = EntrepotJpaEntity.builder()
            .id(entrepot.getId().getValue())
            .code(entrepot.getCode())
            .nom(entrepot.getNom())
            .type(entrepot.getType())
            .regionId(entrepot.getRegionId() != null ? entrepot.getRegionId().getValue() : null)
            .adresse(entrepot.getAdresse())
            .telephone(entrepot.getTelephone())
            .responsableUserId(entrepot.getResponsableUserId())
            .actif(entrepot.isActif())
            .build();
        entity.setCreatedAt(entrepot.getCreatedAt());
        entity.setUpdatedAt(entrepot.getUpdatedAt());
        return entity;
    }
}
