package ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper;

import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneOffreJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.OffreFournisseurJpaEntity;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OffreFournisseurMapper — conversion domaine ↔ entité JPA")
class OffreFournisseurMapperTest {

    OffreFournisseurMapper sut = new OffreFournisseurMapper();

    private LigneOffre ligne() {
        return LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.valueOf(2000), 15);
    }

    private OffreFournisseur offre() {
        return OffreFournisseur.soumettre(AppelOffreId.generate(), FournisseurId.generate(), "Commentaire",
                List.of(ligne()));
    }

    @Test
    @DisplayName("toEntity() reporte fidèlement chaque champ de l'agrégat, hors lignes")
    void toEntity_reporteChaqueChamp() {
        OffreFournisseur offre = offre();

        OffreFournisseurJpaEntity entity = sut.toEntity(offre);

        assertThat(entity.getId()).isEqualTo(offre.getId().getValue());
        assertThat(entity.getAppelOffreId()).isEqualTo(offre.getAppelOffreId().getValue());
        assertThat(entity.getFournisseurId()).isEqualTo(offre.getFournisseurId().getValue());
        assertThat(entity.getCommentaire()).isEqualTo("Commentaire");
        assertThat(entity.getStatut()).isEqualTo(StatutOffre.SOUMISE);
        assertThat(entity.getCreatedAt()).isEqualTo(offre.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(offre.getUpdatedAt());
    }

    @Test
    @DisplayName("toEntityLignes() produit une entité par ligne, rattachée au parent")
    void toEntityLignes_rattacheAuParent() {
        OffreFournisseur offre = offre();

        List<LigneOffreJpaEntity> lignes = sut.toEntityLignes(offre);

        assertThat(lignes).hasSize(1);
        LigneOffreJpaEntity ligneEntity = lignes.get(0);
        assertThat(ligneEntity.getOffreId()).isEqualTo(offre.getId().getValue());
        assertThat(ligneEntity.getLigneAppelOffreId())
                .isEqualTo(offre.getLignes().get(0).getLigneAppelOffreId().getValue());
        assertThat(ligneEntity.getPrixUnitaire()).isEqualByComparingTo("2000");
        assertThat(ligneEntity.getDelaiLivraisonJours()).isEqualTo(15);
    }

    @Test
    @DisplayName("toDomain() reconstruit l'agrégat avec ses lignes depuis les entités")
    void toDomain_reconstruitAvecLignes() {
        UUID offreId = UUID.randomUUID();
        UUID appelOffreId = UUID.randomUUID();
        UUID fournisseurId = UUID.randomUUID();
        UUID ligneAppelOffreId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        OffreFournisseurJpaEntity entity = new OffreFournisseurJpaEntity(offreId, appelOffreId, fournisseurId,
                "Com", StatutOffre.RETENUE);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        LigneOffreJpaEntity ligneEntity = new LigneOffreJpaEntity(UUID.randomUUID(), offreId, ligneAppelOffreId,
                BigDecimal.valueOf(1500), 20);

        OffreFournisseur offre = sut.toDomain(entity, List.of(ligneEntity));

        assertThat(offre.getId().getValue()).isEqualTo(offreId);
        assertThat(offre.getAppelOffreId().getValue()).isEqualTo(appelOffreId);
        assertThat(offre.getFournisseurId().getValue()).isEqualTo(fournisseurId);
        assertThat(offre.getStatut()).isEqualTo(StatutOffre.RETENUE);
        assertThat(offre.getCreatedAt()).isEqualTo(createdAt);
        assertThat(offre.getLignes()).hasSize(1);
        assertThat(offre.getLignes().get(0).getLigneAppelOffreId().getValue()).isEqualTo(ligneAppelOffreId);
        assertThat(offre.getLignes().get(0).getPrixUnitaire()).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("aller-retour toEntity()/toEntityLignes() puis toDomain() préserve l'état")
    void allerRetour_preserveEtat() {
        OffreFournisseur original = offre();

        OffreFournisseur restaure = sut.toDomain(sut.toEntity(original), sut.toEntityLignes(original));

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getFournisseurId()).isEqualTo(original.getFournisseurId());
        assertThat(restaure.getStatut()).isEqualTo(original.getStatut());
        assertThat(restaure.getLignes()).hasSize(1);
        assertThat(restaure.getLignes().get(0).getPrixUnitaire())
                .isEqualByComparingTo(original.getLignes().get(0).getPrixUnitaire());
    }
}
