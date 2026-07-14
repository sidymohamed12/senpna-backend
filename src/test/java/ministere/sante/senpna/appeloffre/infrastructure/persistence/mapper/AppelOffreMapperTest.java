package ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper;

import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.AppelOffreJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneAppelOffreJpaEntity;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppelOffreMapper — conversion domaine ↔ entité JPA")
class AppelOffreMapperTest {

    AppelOffreMapper sut = new AppelOffreMapper();

    private LigneAppelOffre ligne() {
        return LigneAppelOffre.creer(MedicamentId.generate(), "Amoxicilline 500 mg", BigDecimal.TEN, "Comprimé");
    }

    private AppelOffre appelOffre() {
        return AppelOffre.creer("AO-2026-0001", "Achat Amoxicilline", LocalDate.now().plusDays(10),
                List.of(ligne()));
    }

    @Test
    @DisplayName("toEntity() reporte fidèlement chaque champ de l'agrégat, hors lignes")
    void toEntity_reporteChaqueChamp() {
        AppelOffre appelOffre = appelOffre();

        AppelOffreJpaEntity entity = sut.toEntity(appelOffre);

        assertThat(entity.getId()).isEqualTo(appelOffre.getId().getValue());
        assertThat(entity.getReference()).isEqualTo("AO-2026-0001");
        assertThat(entity.getObjet()).isEqualTo("Achat Amoxicilline");
        assertThat(entity.getStatut()).isEqualTo(StatutAppelOffre.BROUILLON);
        assertThat(entity.getCreatedAt()).isEqualTo(appelOffre.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(appelOffre.getUpdatedAt());
    }

    @Test
    @DisplayName("toEntityLignes() produit une entité par ligne, rattachée au parent")
    void toEntityLignes_rattacheAuParent() {
        AppelOffre appelOffre = appelOffre();

        List<LigneAppelOffreJpaEntity> lignes = sut.toEntityLignes(appelOffre);

        assertThat(lignes).hasSize(1);
        LigneAppelOffreJpaEntity ligneEntity = lignes.get(0);
        assertThat(ligneEntity.getAppelOffreId()).isEqualTo(appelOffre.getId().getValue());
        assertThat(ligneEntity.getDesignation()).isEqualTo("Amoxicilline 500 mg");
        assertThat(ligneEntity.getQuantiteEstimee()).isEqualByComparingTo("10");
        assertThat(ligneEntity.getUniteBase()).isEqualTo("Comprimé");
    }

    @Test
    @DisplayName("toDomain() reconstruit l'agrégat avec ses lignes depuis les entités")
    void toDomain_reconstruitAvecLignes() {
        UUID appelOffreId = UUID.randomUUID();
        UUID medicamentId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        AppelOffreJpaEntity entity = new AppelOffreJpaEntity(appelOffreId, "AO-2026-0099", "Objet",
                LocalDate.now().plusDays(5), StatutAppelOffre.PUBLIE);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        LigneAppelOffreJpaEntity ligneEntity = new LigneAppelOffreJpaEntity(UUID.randomUUID(), appelOffreId,
                medicamentId, "Paracétamol", BigDecimal.valueOf(500), "Comprimé");

        AppelOffre appelOffre = sut.toDomain(entity, List.of(ligneEntity));

        assertThat(appelOffre.getId().getValue()).isEqualTo(appelOffreId);
        assertThat(appelOffre.getReference()).isEqualTo("AO-2026-0099");
        assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.PUBLIE);
        assertThat(appelOffre.getCreatedAt()).isEqualTo(createdAt);
        assertThat(appelOffre.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(appelOffre.getLignes()).hasSize(1);
        assertThat(appelOffre.getLignes().get(0).getMedicamentId().getValue()).isEqualTo(medicamentId);
        assertThat(appelOffre.getLignes().get(0).getDesignation()).isEqualTo("Paracétamol");
    }

    @Test
    @DisplayName("toDomain() sans ligne → agrégat sans ligne")
    void toDomain_sansLigne() {
        AppelOffreJpaEntity entity = new AppelOffreJpaEntity(UUID.randomUUID(), "AO-1", "Objet",
                LocalDate.now().plusDays(5), StatutAppelOffre.BROUILLON);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        AppelOffre appelOffre = sut.toDomain(entity, List.of());

        assertThat(appelOffre.getLignes()).isEmpty();
    }

    @Test
    @DisplayName("aller-retour toEntity()/toEntityLignes() puis toDomain() préserve l'état")
    void allerRetour_preserveEtat() {
        AppelOffre original = appelOffre();

        AppelOffre restaure = sut.toDomain(sut.toEntity(original), sut.toEntityLignes(original));

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getReference()).isEqualTo(original.getReference());
        assertThat(restaure.getStatut()).isEqualTo(original.getStatut());
        assertThat(restaure.getLignes()).hasSize(1);
        assertThat(restaure.getLignes().get(0).getDesignation())
                .isEqualTo(original.getLignes().get(0).getDesignation());
    }
}
