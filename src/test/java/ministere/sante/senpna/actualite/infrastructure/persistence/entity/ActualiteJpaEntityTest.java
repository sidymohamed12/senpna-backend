package ministere.sante.senpna.actualite.infrastructure.persistence.entity;

import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ActualiteJpaEntity — construction et gestion des médias")
class ActualiteJpaEntityTest {

        @Test
        @DisplayName("le constructeur complet renseigne tous les champs, avec une liste de tags copiée")
        void constructeur_renseigneTousLesChamps() {
                UUID id = UUID.randomUUID();
                UUID auteurId = UUID.randomUUID();
                List<String> tags = new ArrayList<>(List.of("sante", "senegal"));

                ActualiteJpaEntity entity = new ActualiteJpaEntity(id, CategorieActualite.PROJET, "Titre",
                                "Description",
                                auteurId, "Awa Diop", tags, StatutActualite.BROUILLON);

                assertThat(entity.getId()).isEqualTo(id);
                assertThat(entity.getCategorie()).isEqualTo(CategorieActualite.PROJET);
                assertThat(entity.getTitre()).isEqualTo("Titre");
                assertThat(entity.getDescription()).isEqualTo("Description");
                assertThat(entity.getAuteurId()).isEqualTo(auteurId);
                assertThat(entity.getAuteurNom()).isEqualTo("Awa Diop");
                assertThat(entity.getTags()).containsExactly("sante", "senegal");
                assertThat(entity.getStatut()).isEqualTo(StatutActualite.BROUILLON);
                assertThat(entity.getMedias()).isEmpty();

                // La liste de tags est copiée, pas partagée avec l'appelant.
                tags.add("nouveau");
                assertThat(entity.getTags()).doesNotContain("nouveau");
        }

        @Test
        @DisplayName("le constructeur accepte une liste de tags null et l'initialise à une liste vide")
        void constructeur_tagsNull_listeVide() {
                ActualiteJpaEntity entity = new ActualiteJpaEntity(UUID.randomUUID(), CategorieActualite.AUTRE, "Titre",
                                null, UUID.randomUUID(), "Auteur", null, StatutActualite.BROUILLON);

                assertThat(entity.getTags()).isEmpty();
        }

        @Test
        @DisplayName("remplacerMedias() vide la collection existante puis ajoute les nouveaux médias en fixant le back-reference")
        void remplacerMedias_remplaceEtFixeBackReference() {
                ActualiteJpaEntity entity = new ActualiteJpaEntity(UUID.randomUUID(), CategorieActualite.PROJET,
                                "Titre",
                                null, UUID.randomUUID(), "Auteur", List.of(), StatutActualite.BROUILLON);

                ActualiteMediaJpaEntity ancienMedia = new ActualiteMediaJpaEntity(UUID.randomUUID(), entity,
                                TypeMedia.IMAGE,
                                "https://cdn/ancien.png", 0);
                entity.remplacerMedias(List.of(ancienMedia));
                assertThat(entity.getMedias()).hasSize(1);

                ActualiteMediaJpaEntity nouveauMedia = new ActualiteMediaJpaEntity(UUID.randomUUID(), null,
                                TypeMedia.VIDEO,
                                "https://cdn/nouveau.mp4", 0);

                entity.remplacerMedias(List.of(nouveauMedia));

                assertThat(entity.getMedias()).hasSize(1);
                assertThat(entity.getMedias()).containsExactly(nouveauMedia);
                assertThat(nouveauMedia.getActualite()).isSameAs(entity);
        }

        @Test
        @DisplayName("remplacerMedias(null) vide simplement la collection existante")
        void remplacerMedias_null_videLaCollection() {
                ActualiteJpaEntity entity = new ActualiteJpaEntity(UUID.randomUUID(), CategorieActualite.PROJET,
                                "Titre",
                                null, UUID.randomUUID(), "Auteur", List.of(), StatutActualite.BROUILLON);
                ActualiteMediaJpaEntity media = new ActualiteMediaJpaEntity(UUID.randomUUID(), entity, TypeMedia.IMAGE,
                                "https://cdn/1.png", 0);
                entity.remplacerMedias(List.of(media));
                assertThat(entity.getMedias()).hasSize(1);

                entity.remplacerMedias(null);

                assertThat(entity.getMedias()).isEmpty();
        }

        @Test
        @DisplayName("equals()/hashCode() se basent sur les champs métier hors id, médias et tags")
        void equalsEtHashCode() {
                UUID auteurId = UUID.randomUUID();
                ActualiteJpaEntity entity1 = new ActualiteJpaEntity(UUID.randomUUID(), CategorieActualite.PROJET,
                                "Titre",
                                "Description", auteurId, "Auteur", List.of("a"), StatutActualite.BROUILLON);
                ActualiteJpaEntity entity2 = new ActualiteJpaEntity(UUID.randomUUID(), CategorieActualite.PROJET,
                                "Titre",
                                "Description", auteurId, "Auteur", List.of("b", "c"), StatutActualite.BROUILLON);
                ActualiteJpaEntity entityDifferente = new ActualiteJpaEntity(UUID.randomUUID(),
                                CategorieActualite.AUTRE,
                                "Autre titre", "Description", auteurId, "Auteur", List.of("a"),
                                StatutActualite.BROUILLON);

                assertThat(entity1).isEqualTo(entity1)
                                .isNotEqualTo(null)
                                .isNotEqualTo("pas une entité")
                                .isEqualTo(entity2);
                assertThat(entity1.hashCode()).hasSameClassAs(entity2.hashCode());
                assertThat(entity1).isNotEqualTo(entityDifferente);
        }
}
