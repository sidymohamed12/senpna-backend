package ministere.sante.senpna.actualite.domain.model;

import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Actualite — agrégat de domaine")
class ActualiteTest {

    private static final UUID AUTEUR_ID = UUID.randomUUID();

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une actualité en BROUILLON par défaut")
        void creer_succes_statutBrouillon() {
            Actualite actualite = Actualite.creer(CategorieActualite.VIE_ASSOCIATIVE, "Journée de sensibilisation",
                    "Une belle journée", List.of(), AUTEUR_ID, "Awa Diop", List.of("sante", "senegal"));

            assertThat(actualite.getStatut()).isEqualTo(StatutActualite.BROUILLON);
            assertThat(actualite.getTitre()).isEqualTo("Journée de sensibilisation");
            assertThat(actualite.getAuteurId()).isEqualTo(AUTEUR_ID);
            assertThat(actualite.getTags()).containsExactly("sante", "senegal");
            assertThat(actualite.getId()).isNotNull();
        }

        @Test
        @DisplayName("titre vide → IllegalArgumentException")
        void creer_titreVide_leveException() {
            assertThatThrownBy(() -> Actualite.creer(CategorieActualite.PROJET, "   ", null, List.of(), AUTEUR_ID,
                    "Awa Diop", List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("description de plus de 700 caractères → IllegalArgumentException")
        void creer_descriptionTropLongue_leveException() {
            String descriptionTropLongue = "a".repeat(701);

            assertThatThrownBy(() -> Actualite.creer(CategorieActualite.PROJET, "Titre", descriptionTropLongue,
                    List.of(), AUTEUR_ID, "Awa Diop", List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("catégorie null → NullPointerException")
        void creer_categorieNulle_leveException() {
            assertThatThrownBy(() -> Actualite.creer(null, "Titre", null, List.of(), AUTEUR_ID, "Awa Diop",
                    List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plus de 10 médias → IllegalArgumentException")
        void creer_troPlusDeMedias_leveException() {
            List<ActualiteMedia> medias = java.util.stream.IntStream.range(0, 11)
                    .mapToObj(i -> ActualiteMedia.creer(TypeMedia.IMAGE, "https://cdn.senpna.sn/img-" + i + ".jpg", i))
                    .toList();

            assertThatThrownBy(() -> Actualite.creer(CategorieActualite.PROJET, "Titre", null, medias, AUTEUR_ID,
                    "Awa Diop", List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("publier() / desactiver() / remettreEnBrouillon()")
    class Transitions {

        @Test
        @DisplayName("publier() rend l'actualité PUBLIE")
        void publier_rendPublie() {
            Actualite actualite = actualiteBrouillon();

            actualite.publier();

            assertThat(actualite.getStatut()).isEqualTo(StatutActualite.PUBLIE);
        }

        @Test
        @DisplayName("publier() est idempotent")
        void publier_idempotent() {
            Actualite actualite = actualiteBrouillon();
            actualite.publier();

            actualite.publier();

            assertThat(actualite.getStatut()).isEqualTo(StatutActualite.PUBLIE);
        }

        @Test
        @DisplayName("desactiver() masque une actualité publiée")
        void desactiver_masqueActualitePubliee() {
            Actualite actualite = actualiteBrouillon();
            actualite.publier();

            actualite.desactiver();

            assertThat(actualite.getStatut()).isEqualTo(StatutActualite.DESACTIVE);
        }

        @Test
        @DisplayName("publier() republie une actualité désactivée")
        void publier_republieActualiteDesactivee() {
            Actualite actualite = actualiteBrouillon();
            actualite.publier();
            actualite.desactiver();

            actualite.publier();

            assertThat(actualite.getStatut()).isEqualTo(StatutActualite.PUBLIE);
        }

        @Test
        @DisplayName("remettreEnBrouillon() repasse une actualité publiée en brouillon")
        void remettreEnBrouillon_repasseEnBrouillon() {
            Actualite actualite = actualiteBrouillon();
            actualite.publier();

            actualite.remettreEnBrouillon();

            assertThat(actualite.getStatut()).isEqualTo(StatutActualite.BROUILLON);
        }
    }

    @Nested
    @DisplayName("modifierContenu()")
    class ModifierContenu {

        @Test
        @DisplayName("modifie le titre, la description et les médias")
        void modifie_contenuMisAJour() {
            Actualite actualite = actualiteBrouillon();
            List<ActualiteMedia> nouveauxMedias = List
                    .of(ActualiteMedia.creer(TypeMedia.VIDEO, "https://youtube.com/watch?v=abc", 0));

            actualite.modifierContenu(CategorieActualite.PARTENARIAT, "Nouveau titre", "Nouvelle description",
                    nouveauxMedias, List.of("partenaire"));

            assertThat(actualite.getCategorie()).isEqualTo(CategorieActualite.PARTENARIAT);
            assertThat(actualite.getTitre()).isEqualTo("Nouveau titre");
            assertThat(actualite.getMedias()).hasSize(1);
            assertThat(actualite.getTags()).containsExactly("partenaire");
        }
    }

    private Actualite actualiteBrouillon() {
        return Actualite.creer(CategorieActualite.VIE_ASSOCIATIVE, "Titre", "Description", List.of(), AUTEUR_ID,
                "Awa Diop", List.of());
    }
}
