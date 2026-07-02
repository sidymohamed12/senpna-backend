package ministere.sante.senpna.organisation.infrastructure.persistence.mapper;

import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.RegionJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class RegionMapper {

    public Region toDomain(RegionJpaEntity entity) {
        return Region.reconstruct(
                RegionId.of(entity.getId()),
                entity.getCode(),
                entity.getNom(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public RegionJpaEntity toEntity(Region region) {
        RegionJpaEntity entity = new RegionJpaEntity(
                region.getId().getValue(),
                region.getCode(),
                region.getNom(),
                region.isActif());
        entity.setCreatedAt(region.getCreatedAt());
        entity.setUpdatedAt(region.getUpdatedAt());
        return entity;
    }
}
