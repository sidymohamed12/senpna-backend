package ministere.sante.senpna.carriere.infrastructure.persistence.mapper;

import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.OpportuniteCarriereJpaEntity;

import org.springframework.stereotype.Component;

@Component
public class OpportuniteCarriereMapper {

    public OpportuniteCarriere toDomain(OpportuniteCarriereJpaEntity entity) {
        return OpportuniteCarriere.reconstruct(
                OpportuniteCarriereId.of(entity.getId()),
                entity.getTitre(),
                entity.getNomEntreprise(),
                entity.getDescription(),
                entity.getFicheDePosteUrl(),
                entity.getLieu(),
                entity.getTypeContrat(),
                entity.getDateDebut(),
                entity.getDateLimiteCandidature(),
                entity.getAuteurId(),
                entity.getAuteurNom(),
                entity.getEmailContact(),
                entity.getStatut(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public OpportuniteCarriereJpaEntity toNewEntity(OpportuniteCarriere opportunite) {
        OpportuniteCarriereJpaEntity entity = new OpportuniteCarriereJpaEntity(
                opportunite.getId().getValue(),
                opportunite.getTitre(),
                opportunite.getNomEntreprise(),
                opportunite.getDescription(),
                opportunite.getFicheDePosteUrl(),
                opportunite.getLieu(),
                opportunite.getTypeContrat(),
                opportunite.getDateDebut(),
                opportunite.getDateLimiteCandidature(),
                opportunite.getAuteurId(),
                opportunite.getAuteurNom(),
                opportunite.getEmailContact(),
                opportunite.getStatut());
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
