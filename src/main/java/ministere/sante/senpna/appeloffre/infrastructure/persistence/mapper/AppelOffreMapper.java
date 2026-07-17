package ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper;

import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.AppelOffreJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneAppelOffreJpaEntity;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppelOffreMapper {

    public AppelOffre toDomain(AppelOffreJpaEntity entity, List<LigneAppelOffreJpaEntity> lignesEntity) {
        List<LigneAppelOffre> lignes = lignesEntity.stream().map(this::toDomainLigne).toList();
        return AppelOffre.builder()
            .id(AppelOffreId.of(entity.getId()))
            .reference(entity.getReference())
            .objet(entity.getObjet())
            .dateCloture(entity.getDateCloture())
            .statut(entity.getStatut())
            .lignes(lignes)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public AppelOffreJpaEntity toEntity(AppelOffre appelOffre) {
        AppelOffreJpaEntity entity = new AppelOffreJpaEntity(
                appelOffre.getId().getValue(),
                appelOffre.getReference(),
                appelOffre.getObjet(),
                appelOffre.getDateCloture(),
                appelOffre.getStatut());
        entity.setCreatedAt(appelOffre.getCreatedAt());
        entity.setUpdatedAt(appelOffre.getUpdatedAt());
        return entity;
    }

    public List<LigneAppelOffreJpaEntity> toEntityLignes(AppelOffre appelOffre) {
        return appelOffre.getLignes().stream()
                .map(ligne -> new LigneAppelOffreJpaEntity(
                        ligne.getId().getValue(),
                        appelOffre.getId().getValue(),
                        ligne.getMedicamentId().getValue(),
                        ligne.getDesignation(),
                        ligne.getQuantiteEstimee(),
                        ligne.getUniteBase()))
                .toList();
    }

    private LigneAppelOffre toDomainLigne(LigneAppelOffreJpaEntity entity) {
        return LigneAppelOffre.reconstruct(
                LigneAppelOffreId.of(entity.getId()),
                MedicamentId.of(entity.getMedicamentId()),
                entity.getDesignation(),
                entity.getQuantiteEstimee(),
                entity.getUniteBase());
    }
}
