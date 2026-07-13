package ministere.sante.senpna.projet.infrastructure.persistence.mapper;

import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.projet.infrastructure.persistence.entity.ProjetJpaEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProjetMapper — conversion domaine ↔ entité JPA")
class ProjetMapperTest {

    ProjetMapper sut = new ProjetMapper();

    @Test
    @DisplayName("toDomain() reporte fidèlement chaque champ, y compris les listes")
    void toDomain_reporteChaqueChamp() {
        UUID id = UUID.randomUUID();
        ProjetJpaEntity entity = new ProjetJpaEntity(id, CategorieProjet.SANTE, "Nom", "Desc",
                List.of("O1", "O2"), List.of("I1"), "img", StatutProjet.PUBLIE);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        Projet projet = sut.toDomain(entity);

        assertThat(projet.getId().getValue()).isEqualTo(id);
        assertThat(projet.getCategorie()).isEqualTo(CategorieProjet.SANTE);
        assertThat(projet.getObjectifs()).containsExactly("O1", "O2");
        assertThat(projet.getImpacts()).containsExactly("I1");
        assertThat(projet.getStatut()).isEqualTo(StatutProjet.PUBLIE);
    }

    @Test
    @DisplayName("toEntity() reporte fidèlement chaque champ de l'agrégat")
    void toEntity_reporteChaqueChamp() {
        Projet projet = Projet.creer(CategorieProjet.INNOVATION, "Nom", "Desc", List.of("O"), List.of("I"), "img");

        ProjetJpaEntity entity = sut.toEntity(projet);

        assertThat(entity.getId()).isEqualTo(projet.getId().getValue());
        assertThat(entity.getCategorie()).isEqualTo(CategorieProjet.INNOVATION);
        assertThat(entity.getObjectifs()).containsExactly("O");
        assertThat(entity.getStatut()).isEqualTo(StatutProjet.BROUILLON);
        assertThat(entity.getCreatedAt()).isEqualTo(projet.getCreatedAt());
    }

    @Test
    @DisplayName("aller-retour préserve l'état du projet")
    void allerRetour_preserveEtat() {
        Projet original = Projet.creer(CategorieProjet.EDUCATION, "Nom", "Desc", List.of("O"), List.of("I"), null);

        Projet restaure = sut.toDomain(sut.toEntity(original));

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getNom()).isEqualTo(original.getNom());
        assertThat(restaure.getObjectifs()).isEqualTo(original.getObjectifs());
    }
}