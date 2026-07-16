package ministere.sante.senpna.actualite.infrastructure.persistence.mapper;

import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteJpaEntity;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteMediaJpaEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ActualiteMapper — conversion entre le domaine et les entités JPA")
class ActualiteMapperTest {

    private final ActualiteMapper mapper = new ActualiteMapper();

    private static final UUID ACTUALITE_ID = UUID.randomUUID();
    private static final UUID AUTEUR_ID = UUID.randomUUID();

    @Test
    @DisplayName("toDomain() convertit une entité JPA, y compris ses médias, vers le domaine")
    void toDomain_convertitEntiteEtMedias() {
        ActualiteJpaEntity entity = new ActualiteJpaEntity(ACTUALITE_ID, CategorieActualite.PROJET, "Titre",
                "Description", AUTEUR_ID, "Awa Diop", List.of("sante"), StatutActualite.PUBLIE);
        Instant maintenant = Instant.now();
        entity.setCreatedAt(maintenant);
        entity.setUpdatedAt(maintenant);
        ActualiteMediaJpaEntity mediaEntity = new ActualiteMediaJpaEntity(UUID.randomUUID(), entity, TypeMedia.IMAGE,
                "https://cdn/1.png", 0);
        entity.remplacerMedias(List.of(mediaEntity));

        Actualite actualite = mapper.toDomain(entity);

        assertThat(actualite.getId().getValue()).isEqualTo(ACTUALITE_ID);
        assertThat(actualite.getCategorie()).isEqualTo(CategorieActualite.PROJET);
        assertThat(actualite.getTitre()).isEqualTo("Titre");
        assertThat(actualite.getDescription()).isEqualTo("Description");
        assertThat(actualite.getMedias()).hasSize(1);
        assertThat(actualite.getMedias().get(0).getUrl()).isEqualTo("https://cdn/1.png");
        assertThat(actualite.getAuteurId()).isEqualTo(AUTEUR_ID);
        assertThat(actualite.getAuteurNom()).isEqualTo("Awa Diop");
        assertThat(actualite.getTags()).containsExactly("sante");
        assertThat(actualite.getStatut()).isEqualTo(StatutActualite.PUBLIE);
        assertThat(actualite.getCreatedAt()).isEqualTo(maintenant);
        assertThat(actualite.getUpdatedAt()).isEqualTo(maintenant);
    }

    @Test
    @DisplayName("toNewEntity() construit une nouvelle entité JPA à partir de l'agrégat, avec ses médias")
    void toNewEntity_construitEntiteAvecMedias() {
        Instant maintenant = Instant.now();
        ActualiteMedia media = ActualiteMedia.reconstruct(UUID.randomUUID(), TypeMedia.VIDEO, "https://cdn/1.mp4", 0);
        Actualite actualite = Actualite.reconstruct(ActualiteId.of(ACTUALITE_ID), CategorieActualite.EVENEMENT,
                "Titre", "Description", List.of(media), AUTEUR_ID, "Awa Diop", List.of("evenement"),
                StatutActualite.BROUILLON, maintenant, maintenant);

        ActualiteJpaEntity entity = mapper.toNewEntity(actualite);

        assertThat(entity.getId()).isEqualTo(ACTUALITE_ID);
        assertThat(entity.getCategorie()).isEqualTo(CategorieActualite.EVENEMENT);
        assertThat(entity.getTitre()).isEqualTo("Titre");
        assertThat(entity.getDescription()).isEqualTo("Description");
        assertThat(entity.getAuteurId()).isEqualTo(AUTEUR_ID);
        assertThat(entity.getAuteurNom()).isEqualTo("Awa Diop");
        assertThat(entity.getTags()).containsExactly("evenement");
        assertThat(entity.getStatut()).isEqualTo(StatutActualite.BROUILLON);
        assertThat(entity.getCreatedAt()).isEqualTo(maintenant);
        assertThat(entity.getUpdatedAt()).isEqualTo(maintenant);
        assertThat(entity.getMedias()).hasSize(1);
        assertThat(entity.getMedias().get(0).getUrl()).isEqualTo("https://cdn/1.mp4");
        assertThat(entity.getMedias().get(0).getActualite()).isSameAs(entity);
    }

    @Test
    @DisplayName("updateEntity() met à jour une entité managée existante en préservant sa collection de médias")
    void updateEntity_metAJourEntiteExistante() {
        ActualiteJpaEntity entity = new ActualiteJpaEntity(ACTUALITE_ID, CategorieActualite.PROJET, "Ancien titre",
                "Ancienne description", AUTEUR_ID, "Ancien nom", List.of("ancien"), StatutActualite.BROUILLON);
        Instant creation = Instant.now().minusSeconds(3600);
        entity.setCreatedAt(creation);
        entity.setUpdatedAt(creation);

        Instant maintenant = Instant.now();
        ActualiteMedia nouveauMedia = ActualiteMedia.reconstruct(UUID.randomUUID(), TypeMedia.IMAGE,
                "https://cdn/nouveau.png", 0);
        Actualite actualite = Actualite.reconstruct(ActualiteId.of(ACTUALITE_ID), CategorieActualite.PARTENARIAT,
                "Nouveau titre", "Nouvelle description", List.of(nouveauMedia), AUTEUR_ID, "Nouveau nom",
                List.of("nouveau"), StatutActualite.PUBLIE, creation, maintenant);

        ActualiteJpaEntity result = mapper.updateEntity(entity, actualite);

        assertThat(result).isSameAs(entity);
        assertThat(result.getCategorie()).isEqualTo(CategorieActualite.PARTENARIAT);
        assertThat(result.getTitre()).isEqualTo("Nouveau titre");
        assertThat(result.getDescription()).isEqualTo("Nouvelle description");
        assertThat(result.getAuteurNom()).isEqualTo("Nouveau nom");
        assertThat(result.getTags()).containsExactly("nouveau");
        assertThat(result.getStatut()).isEqualTo(StatutActualite.PUBLIE);
        assertThat(result.getUpdatedAt()).isEqualTo(maintenant);
        assertThat(result.getCreatedAt()).isEqualTo(creation);
        assertThat(result.getMedias()).hasSize(1);
        assertThat(result.getMedias().get(0).getUrl()).isEqualTo("https://cdn/nouveau.png");
    }

    @Test
    @DisplayName("toNewEntity() sans média produit une entité sans média")
    void toNewEntity_sansMedia() {
        Instant maintenant = Instant.now();
        Actualite actualite = Actualite.reconstruct(ActualiteId.of(ACTUALITE_ID), CategorieActualite.AUTRE, "Titre",
                null, List.of(), AUTEUR_ID, "Auteur", List.of(), StatutActualite.BROUILLON, maintenant, maintenant);

        ActualiteJpaEntity entity = mapper.toNewEntity(actualite);

        assertThat(entity.getMedias()).isEmpty();
    }
}
