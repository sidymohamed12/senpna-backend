package ministere.sante.senpna.organisation.infrastructure.persistence.mapper;

import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.EntrepotJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class EntrepotMapper {

    public Entrepot toDomain(EntrepotJpaEntity entity) {
        return Entrepot.reconstruct(
                EntrepotId.of(entity.getId()),
                entity.getCode(),
                entity.getNom(),
                entity.getType(),
                entity.getRegionId() != null ? RegionId.of(entity.getRegionId()) : null,
                entity.getAdresse(),
                entity.getTelephone(),
                entity.getResponsableUserId(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public EntrepotJpaEntity toEntity(Entrepot entrepot) {
        EntrepotJpaEntity entity = new EntrepotJpaEntity(
                entrepot.getId().getValue(),
                entrepot.getCode(),
                entrepot.getNom(),
                entrepot.getType(),
                entrepot.getRegionId() != null ? entrepot.getRegionId().getValue() : null,
                entrepot.getAdresse(),
                entrepot.getTelephone(),
                entrepot.getResponsableUserId(),
                entrepot.isActif());
        entity.setCreatedAt(entrepot.getCreatedAt());
        entity.setUpdatedAt(entrepot.getUpdatedAt());
        return entity;
    }
}
