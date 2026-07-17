package ministere.sante.senpna.carriere.infrastructure.persistence.mapper;

import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.OpportuniteCarriereJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class OpportuniteCarriereMapper {

    public OpportuniteCarriere toDomain(OpportuniteCarriereJpaEntity entity) {
        return OpportuniteCarriere.builder()
            .id(OpportuniteCarriereId.of(entity.getId()))
            .titre(entity.getTitre())
            .nomEntreprise(entity.getNomEntreprise())
            .description(entity.getDescription())
            .ficheDePosteUrl(entity.getFicheDePosteUrl())
            .lieu(entity.getLieu())
            .typeContrat(entity.getTypeContrat())
            .dateDebut(entity.getDateDebut())
            .dateLimiteCandidature(entity.getDateLimiteCandidature())
            .auteurId(entity.getAuteurId())
            .auteurNom(entity.getAuteurNom())
            .emailContact(entity.getEmailContact())
            .statut(entity.getStatut())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public OpportuniteCarriereJpaEntity toNewEntity(OpportuniteCarriere opportunite) {
        OpportuniteCarriereJpaEntity entity = OpportuniteCarriereJpaEntity.builder()
            .id(opportunite.getId().getValue())
            .titre(opportunite.getTitre())
            .nomEntreprise(opportunite.getNomEntreprise())
            .description(opportunite.getDescription())
            .ficheDePosteUrl(opportunite.getFicheDePosteUrl())
            .lieu(opportunite.getLieu())
            .typeContrat(opportunite.getTypeContrat())
            .dateDebut(opportunite.getDateDebut())
            .dateLimiteCandidature(opportunite.getDateLimiteCandidature())
            .auteurId(opportunite.getAuteurId())
            .auteurNom(opportunite.getAuteurNom())
            .emailContact(opportunite.getEmailContact())
            .statut(opportunite.getStatut())
            .build();
        entity.setCreatedAt(opportunite.getCreatedAt());
        entity.setUpdatedAt(opportunite.getUpdatedAt());
        return entity;
    }

    public OpportuniteCarriereJpaEntity updateEntity(OpportuniteCarriereJpaEntity entity,
            OpportuniteCarriere opportunite) {
        entity.setTitre(opportunite.getTitre());
        entity.setNomEntreprise(opportunite.getNomEntreprise());
        entity.setDescription(opportunite.getDescription());
        entity.setFicheDePosteUrl(opportunite.getFicheDePosteUrl());
        entity.setLieu(opportunite.getLieu());
        entity.setTypeContrat(opportunite.getTypeContrat());
        entity.setDateDebut(opportunite.getDateDebut());
        entity.setDateLimiteCandidature(opportunite.getDateLimiteCandidature());
        entity.setAuteurId(opportunite.getAuteurId());
        entity.setAuteurNom(opportunite.getAuteurNom());
        entity.setEmailContact(opportunite.getEmailContact());
        entity.setStatut(opportunite.getStatut());
        entity.setUpdatedAt(opportunite.getUpdatedAt());
        return entity;
    }
}
