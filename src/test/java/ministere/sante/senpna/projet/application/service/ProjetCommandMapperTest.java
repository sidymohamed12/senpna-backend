package ministere.sante.senpna.projet.application.service;

import ministere.sante.senpna.projet.domain.exception.CategorieProjetInvalideException;
import ministere.sante.senpna.projet.domain.exception.StatutProjetInvalideException;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProjetCommandMapper — conversion des valeurs de commande vers les enums")
class ProjetCommandMapperTest {

    ProjetCommandMapper sut = new ProjetCommandMapper();

    @Nested
    @DisplayName("versCategorie()")
    class VersCategorie {

        @Test
        @DisplayName("valeur valide, insensible à la casse et aux espaces")
        void valeurValide() {
            assertThat(sut.versCategorie(" innovation ")).isEqualTo(CategorieProjet.INNOVATION);
        }

        @Test
        @DisplayName("valeur null → CategorieProjetInvalideException")
        void valeurNull_leveException() {
            assertThatThrownBy(() -> sut.versCategorie(null)).isInstanceOf(CategorieProjetInvalideException.class);
        }

        @Test
        @DisplayName("valeur vide/blanche → CategorieProjetInvalideException")
        void valeurVide_leveException() {
            assertThatThrownBy(() -> sut.versCategorie("  ")).isInstanceOf(CategorieProjetInvalideException.class);
        }

        @Test
        @DisplayName("valeur hors énumération → CategorieProjetInvalideException")
        void valeurHorsEnumeration_leveException() {
            assertThatThrownBy(() -> sut.versCategorie("INEXISTANTE"))
                    .isInstanceOf(CategorieProjetInvalideException.class);
        }
    }

    @Nested
    @DisplayName("versCategorieOptionnelle()")
    class VersCategorieOptionnelle {

        @Test
        @DisplayName("null → null, aucune exception")
        void null_renvoieNull() {
            assertThat(sut.versCategorieOptionnelle(null)).isNull();
        }

        @Test
        @DisplayName("vide → null")
        void vide_renvoieNull() {
            assertThat(sut.versCategorieOptionnelle("")).isNull();
        }

        @Test
        @DisplayName("valeur valide → catégorie résolue")
        void valeurValide_resolue() {
            assertThat(sut.versCategorieOptionnelle("SANTE")).isEqualTo(CategorieProjet.SANTE);
        }

        @Test
        @DisplayName("valeur invalide non vide → lève quand même l'exception")
        void valeurInvalideNonVide_leveException() {
            assertThatThrownBy(() -> sut.versCategorieOptionnelle("INEXISTANTE"))
                    .isInstanceOf(CategorieProjetInvalideException.class);
        }
    }

    @Nested
    @DisplayName("versStatutOptionnel()")
    class VersStatutOptionnel {

        @Test
        @DisplayName("null → null")
        void null_renvoieNull() {
            assertThat(sut.versStatutOptionnel(null)).isNull();
        }

        @Test
        @DisplayName("valeur valide → statut résolu")
        void valeurValide_resolue() {
            assertThat(sut.versStatutOptionnel("publie")).isEqualTo(StatutProjet.PUBLIE);
        }

        @Test
        @DisplayName("valeur invalide → StatutProjetInvalideException")
        void valeurInvalide_leveException() {
            assertThatThrownBy(() -> sut.versStatutOptionnel("INEXISTANT"))
                    .isInstanceOf(StatutProjetInvalideException.class);
        }
    }
}
