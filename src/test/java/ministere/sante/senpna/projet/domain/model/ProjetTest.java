package ministere.sante.senpna.projet.domain.model;

import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Projet — agrégat de domaine")
class ProjetTest {

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée un projet en BROUILLON par défaut")
        void creer_succes_statutBrouillon() {
            Projet projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Accès aux soins ruraux",
                    "Amélioration de l'accès aux médicaments essentiels", List.of("Réduire les ruptures"),
                    List.of("+20% de disponibilité"), "https://cdn.senpna.sn/projets/soins-ruraux.jpg"));

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.BROUILLON);
            assertThat(projet.getNom()).isEqualTo("Accès aux soins ruraux");
            assertThat(projet.getObjectifs()).containsExactly("Réduire les ruptures");
            assertThat(projet.getImpacts()).containsExactly("+20% de disponibilité");
            assertThat(projet.getId()).isNotNull();
        }

        @Test
        @DisplayName("nom vide → IllegalArgumentException")
        void creer_nomVide_leveException() {
            assertThatThrownBy(() -> Projet.creer(new Projet.CreationCommand(CategorieProjet.SOCIAL, "   ", null, List.of(), List.of(), null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("description de plus de 700 caractères → IllegalArgumentException")
        void creer_descriptionTropLongue_leveException() {
            String descriptionTropLongue = "a".repeat(701);

            assertThatThrownBy(() -> Projet.creer(new Projet.CreationCommand(CategorieProjet.SOCIAL, "Nom", descriptionTropLongue, List.of(),
                    List.of(), null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("catégorie null → NullPointerException")
        void creer_categorieNulle_leveException() {
            assertThatThrownBy(() -> Projet.creer(new Projet.CreationCommand(null, "Nom", null, List.of(), List.of(), null)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plus de 10 objectifs → IllegalArgumentException")
        void creer_tropDObjectifs_leveException() {
            List<String> objectifs = java.util.stream.IntStream.range(0, 11)
                    .mapToObj(i -> "Objectif " + i)
                    .toList();

            assertThatThrownBy(
                    () -> Projet.creer(new Projet.CreationCommand(CategorieProjet.INNOVATION, "Nom", null, objectifs, List.of(), null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("publier() / archiver() / desactiver() / remettreEnBrouillon()")
    class Transitions {

        @Test
        @DisplayName("publier() rend le projet PUBLIE")
        void publier_rendPublie() {
            Projet projet = projetBrouillon();

            projet.publier();

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.PUBLIE);
        }

        @Test
        @DisplayName("publier() est idempotent")
        void publier_idempotent() {
            Projet projet = projetBrouillon();
            projet.publier();

            projet.publier();

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.PUBLIE);
        }

        @Test
        @DisplayName("archiver() clôture un projet publié")
        void archiver_clotureProjetPublie() {
            Projet projet = projetBrouillon();
            projet.publier();

            projet.archiver();

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.ARCHIVE);
        }

        @Test
        @DisplayName("desactiver() masque un projet publié")
        void desactiver_masqueProjetPublie() {
            Projet projet = projetBrouillon();
            projet.publier();

            projet.desactiver();

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.DESACTIVE);
        }

        @Test
        @DisplayName("publier() republie un projet désactivé")
        void publier_republieProjetDesactive() {
            Projet projet = projetBrouillon();
            projet.publier();
            projet.desactiver();

            projet.publier();

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.PUBLIE);
        }

        @Test
        @DisplayName("remettreEnBrouillon() repasse un projet archivé en brouillon")
        void remettreEnBrouillon_repasseEnBrouillon() {
            Projet projet = projetBrouillon();
            projet.publier();
            projet.archiver();

            projet.remettreEnBrouillon();

            assertThat(projet.getStatut()).isEqualTo(StatutProjet.BROUILLON);
        }
    }

    @Nested
    @DisplayName("modifierContenu()")
    class ModifierContenu {

        @Test
        @DisplayName("modifie le nom, la catégorie et les listes d'objectifs/impacts")
        void modifie_contenuMisAJour() {
            Projet projet = projetBrouillon();

            projet.modifierContenu(CategorieProjet.INNOVATION, "Nouveau nom", "Nouvelle description",
                    List.of("Nouvel objectif"), List.of("Nouvel impact"), null);

            assertThat(projet.getCategorie()).isEqualTo(CategorieProjet.INNOVATION);
            assertThat(projet.getNom()).isEqualTo("Nouveau nom");
            assertThat(projet.getObjectifs()).containsExactly("Nouvel objectif");
            assertThat(projet.getImpacts()).containsExactly("Nouvel impact");
            assertThat(projet.getImageUrl()).isNull();
        }
    }

    private Projet projetBrouillon() {
        return Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Nom", "Description", List.of(), List.of(), null));
    }
}
