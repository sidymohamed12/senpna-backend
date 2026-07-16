package ministere.sante.senpna.actualite.infrastructure.persistence.entity;

import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ActualiteMediaJpaEntity — construction et accesseurs")
class ActualiteMediaJpaEntityTest {

        @Test
        @DisplayName("le constructeur renseigne tous les champs, y compris le lien vers l'actualité parente")
        void constructeur_renseigneTousLesChamps() {
                UUID id = UUID.randomUUID();
                ActualiteJpaEntity actualite = new ActualiteJpaEntity(UUID.randomUUID(), CategorieActualite.PROJET,
                                "Titre",
                                null, UUID.randomUUID(), "Auteur", List.of(), StatutActualite.BROUILLON);

                ActualiteMediaJpaEntity media = new ActualiteMediaJpaEntity(id, actualite, TypeMedia.IMAGE,
                                "https://cdn/1.png", 3);

                assertThat(media.getId()).isEqualTo(id);
                assertThat(media.getActualite()).isSameAs(actualite);
                assertThat(media.getType()).isEqualTo(TypeMedia.IMAGE);
                assertThat(media.getUrl()).isEqualTo("https://cdn/1.png");
                assertThat(media.getOrdre()).isEqualTo(3);
        }

        @Test
        @DisplayName("le setActualite() permet de rattacher le média après coup, sans impacter equals/hashCode")
        void setActualite_excluDeEqualsEtHashCode() {
                ActualiteMediaJpaEntity media1 = new ActualiteMediaJpaEntity(UUID.randomUUID(), null, TypeMedia.VIDEO,
                                "https://cdn/1.mp4", 0);
                ActualiteJpaEntity actualite = new ActualiteJpaEntity(UUID.randomUUID(), CategorieActualite.PROJET,
                                "Titre",
                                null, UUID.randomUUID(), "Auteur", List.of(), StatutActualite.BROUILLON);

                media1.setActualite(actualite);

                assertThat(media1.getActualite()).isSameAs(actualite);
                assertThat(media1).isEqualTo(media1)
                                .isNotEqualTo(null)
                                .isNotEqualTo("pas un média");
        }
}
