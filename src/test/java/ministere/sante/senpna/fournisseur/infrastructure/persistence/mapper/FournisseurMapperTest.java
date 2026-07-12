package ministere.sante.senpna.fournisseur.infrastructure.persistence.mapper;

import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FournisseurMapper — conversion domaine ↔ entité JPA")
class FournisseurMapperTest {

    FournisseurMapper sut = new FournisseurMapper();

    @Test
    @DisplayName("toDomain() reporte fidèlement chaque champ de l'entité")
    void toDomain_reporteChaqueChamp() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();
        FournisseurJpaEntity entity = new FournisseurJpaEntity(id, "Pharma Plus", "Dakar", "+221771234567",
                "c@p.sn", "Awa", true);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        Fournisseur fournisseur = sut.toDomain(entity);

        assertThat(fournisseur.getId().getValue()).isEqualTo(id);
        assertThat(fournisseur.getNom()).isEqualTo("Pharma Plus");
        assertThat(fournisseur.getAdresse()).isEqualTo("Dakar");
        assertThat(fournisseur.getTelephone()).isEqualTo("+221771234567");
        assertThat(fournisseur.getEmail()).isEqualTo("c@p.sn");
        assertThat(fournisseur.getContactPrincipal()).isEqualTo("Awa");
        assertThat(fournisseur.isActif()).isTrue();
        assertThat(fournisseur.getCreatedAt()).isEqualTo(createdAt);
        assertThat(fournisseur.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("toEntity() reporte fidèlement chaque champ de l'agrégat")
    void toEntity_reporteChaqueChamp() {
        Fournisseur fournisseur = Fournisseur.creer("Pharma Plus", "Dakar", "+221771234567", "c@p.sn", "Awa");

        FournisseurJpaEntity entity = sut.toEntity(fournisseur);

        assertThat(entity.getId()).isEqualTo(fournisseur.getId().getValue());
        assertThat(entity.getNom()).isEqualTo("Pharma Plus");
        assertThat(entity.isActif()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(fournisseur.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(fournisseur.getUpdatedAt());
    }

    @Test
    @DisplayName("aller-retour toEntity() puis toDomain() préserve l'état")
    void allerRetour_preserveEtat() {
        Fournisseur original = Fournisseur.creer("Pharma Plus", "Dakar", "+221771234567", "c@p.sn", "Awa");

        Fournisseur restaure = sut.toDomain(sut.toEntity(original));

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getNom()).isEqualTo(original.getNom());
        assertThat(restaure.isActif()).isEqualTo(original.isActif());
    }
}
