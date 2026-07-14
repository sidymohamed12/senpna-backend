package ministere.sante.senpna.shared.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SenPnaException — racine des exceptions métier, catégorisée par ErrorCategory")
class SenPnaExceptionTest {

    @Test
    @DisplayName("expose le message, le type et la catégorie fournis au constructeur")
    void exposeMessageTypeEtCategorie() {
        SenPnaException ex = new SenPnaException("Ressource introuvable", "RESOURCE_NOT_FOUND",
                ErrorCategory.NOT_FOUND);

        assertThat(ex.getMessage()).isEqualTo("Ressource introuvable");
        assertThat(ex.getType()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(ex.getCategory()).isEqualTo(ErrorCategory.NOT_FOUND);
    }

    @Test
    @DisplayName("catégorie null → NullPointerException, la catégorie est obligatoire")
    void categorieNull_leveException() {
        assertThatThrownBy(() -> new SenPnaException("msg", "TYPE", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("est une RuntimeException — non checked, cohérent avec l'usage transversal en couche application/domaine")
    void estRuntimeException() {
        assertThat(new SenPnaException("msg", "TYPE", ErrorCategory.VALIDATION))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("instanciable directement (concrète) pour un cas d'erreur ponctuel sans sous-classe dédiée")
    void instanciableDirectement() {
        // Ne doit pas lancer d'exception de compilation : SenPnaException n'est plus
        // abstract depuis la suppression des 6 sous-classes intermédiaires
        // (cf. ErrorCategory) — elle peut être levée directement.
        SenPnaException ex = new SenPnaException("Cas ponctuel", "AD_HOC", ErrorCategory.BUSINESS_RULE);

        assertThat(ex.getCategory()).isEqualTo(ErrorCategory.BUSINESS_RULE);
    }

    @Test
    @DisplayName("une exception métier concrète (sous-classe nommée) hérite directement de SenPnaException, "
            + "sans passer par une catégorie intermédiaire — profondeur d'héritage constante (cf. règle S110)")
    void sousClasseNommee_heriteDirectement() {
        SenPnaException ex = new UserNotFoundException();

        assertThat(ex.getClass().getSuperclass()).isEqualTo(SenPnaException.class);
        assertThat(ex.getCategory()).isEqualTo(ErrorCategory.NOT_FOUND);
    }
}
