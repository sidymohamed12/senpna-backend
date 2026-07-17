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
        return StructureSanitaire.builder()
            .id(StructureSanitaireId.of(entity.getId()))
            .code(entity.getCode())
            .nom(entity.getNom())
            .type(entity.getType())
            .regionId(entity.getRegionId() != null ? RegionId.of(entity.getRegionId()) : null)
            .praId(entity.getPraId() != null ? EntrepotId.of(entity.getPraId()) : null)
            .district(entity.getDistrict())
            .adresse(entity.getAdresse())
            .telephone(entity.getTelephone())
            .email(entity.getEmail())
            .responsableNom(entity.getResponsableNom())
            .responsablePrenom(entity.getResponsablePrenom())
            .statutAdhesion(entity.getStatutAdhesion())
            .motifRejet(entity.getMotifRejet())
            .actif(entity.isActif())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public StructureSanitaireJpaEntity toEntity(StructureSanitaire structure) {
        StructureSanitaireJpaEntity entity = StructureSanitaireJpaEntity.builder()
            .id(structure.getId().getValue())
            .code(structure.getCode())
            .nom(structure.getNom())
            .type(structure.getType())
            .regionId(structure.getRegionId() != null ? structure.getRegionId().getValue() : null)
            .praId(structure.getPraId() != null ? structure.getPraId().getValue() : null)
            .district(structure.getDistrict())
            .adresse(structure.getAdresse())
            .telephone(structure.getTelephone())
            .email(structure.getEmail())
            .responsableNom(structure.getResponsableNom())
            .responsablePrenom(structure.getResponsablePrenom())
            .statutAdhesion(structure.getStatutAdhesion())
            .motifRejet(structure.getMotifRejet())
            .actif(structure.isActif())
            .build();
        entity.setCreatedAt(structure.getCreatedAt());
        entity.setUpdatedAt(structure.getUpdatedAt());
        return entity;
    }
}
