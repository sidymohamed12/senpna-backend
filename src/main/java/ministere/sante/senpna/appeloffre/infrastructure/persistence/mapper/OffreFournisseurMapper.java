package ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper;

import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneOffreJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.OffreFournisseurJpaEntity;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OffreFournisseurMapper {

    public OffreFournisseur toDomain(OffreFournisseurJpaEntity entity, List<LigneOffreJpaEntity> lignesEntity) {
        List<LigneOffre> lignes = lignesEntity.stream().map(this::toDomainLigne).toList();
        return OffreFournisseur.builder()
            .id(OffreFournisseurId.of(entity.getId()))
            .appelOffreId(AppelOffreId.of(entity.getAppelOffreId()))
            .fournisseurId(FournisseurId.of(entity.getFournisseurId()))
            .commentaire(entity.getCommentaire())
            .statut(entity.getStatut())
            .lignes(lignes)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public OffreFournisseurJpaEntity toEntity(OffreFournisseur offre) {
        OffreFournisseurJpaEntity entity = new OffreFournisseurJpaEntity(
                offre.getId().getValue(),
                offre.getAppelOffreId().getValue(),
                offre.getFournisseurId().getValue(),
                offre.getCommentaire(),
                offre.getStatut());
        entity.setCreatedAt(offre.getCreatedAt());
        entity.setUpdatedAt(offre.getUpdatedAt());
        return entity;
    }

    public List<LigneOffreJpaEntity> toEntityLignes(OffreFournisseur offre) {
        return offre.getLignes().stream()
                .map(ligne -> new LigneOffreJpaEntity(
                        ligne.getId().getValue(),
                        offre.getId().getValue(),
                        ligne.getLigneAppelOffreId().getValue(),
                        ligne.getPrixUnitaire(),
                        ligne.getDelaiLivraisonJours()))
                .toList();
    }

    private LigneOffre toDomainLigne(LigneOffreJpaEntity entity) {
        return LigneOffre.reconstruct(
                LigneOffreId.of(entity.getId()),
                LigneAppelOffreId.of(entity.getLigneAppelOffreId()),
                entity.getPrixUnitaire(),
                entity.getDelaiLivraisonJours());
    }
}
