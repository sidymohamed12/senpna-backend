package ministere.sante.senpna.organisation.infrastructure.persistence.mapper;

import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.infrastructure.persistence.entity.StructureSanitaireJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class StructureSanitaireMapper {

    public StructureSanitaire toDomain(StructureSanitaireJpaEntity entity) {
        return StructureSanitaire.reconstruct(
                StructureSanitaireId.of(entity.getId()),
                entity.getCode(),
                entity.getNom(),
                entity.getType(),
                entity.getRegionId() != null ? RegionId.of(entity.getRegionId()) : null,
                entity.getPraId() != null ? EntrepotId.of(entity.getPraId()) : null,
                entity.getDistrict(),
                entity.getAdresse(),
                entity.getTelephone(),
                entity.getEmail(),
                entity.getResponsableNom(),
                entity.getResponsablePrenom(),
                entity.getStatutAdhesion(),
                entity.getMotifRejet(),
                entity.isActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public StructureSanitaireJpaEntity toEntity(StructureSanitaire structure) {
        StructureSanitaireJpaEntity entity = new StructureSanitaireJpaEntity(
                structure.getId().getValue(),
                structure.getCode(),
                structure.getNom(),
                structure.getType(),
                structure.getRegionId() != null ? structure.getRegionId().getValue() : null,
                structure.getPraId() != null ? structure.getPraId().getValue() : null,
                structure.getDistrict(),
                structure.getAdresse(),
                structure.getTelephone(),
                structure.getEmail(),
                structure.getResponsableNom(),
                structure.getResponsablePrenom(),
                structure.getStatutAdhesion(),
                structure.getMotifRejet(),
                structure.isActif());
        entity.setCreatedAt(structure.getCreatedAt());
        entity.setUpdatedAt(structure.getUpdatedAt());
        return entity;
    }
}
