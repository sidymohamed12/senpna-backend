package ministere.sante.senpna.actualite.infrastructure.web.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateActualiteRequest — validation Bean Validation (@NoHtml notamment)")
class UpdateActualiteRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("une requête entièrement valide ne lève aucune violation")
    void requeteValide_aucuneViolation() {
        UpdateActualiteRequest requete = new UpdateActualiteRequest(
                "PROJET", "Titre mis à jour", "Description mise à jour.", null, null);

        assertThat(validator.validate(requete)).isEmpty();
    }

    @Test
    @DisplayName("titre contenant une balise HTML est rejeté")
    void titre_avecBaliseHtml_rejete() {
        UpdateActualiteRequest requete = new UpdateActualiteRequest(
                "PROJET", "<b>Titre</b>", "description ok", null, null);

        Set<ConstraintViolation<UpdateActualiteRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("titre");
    }

    @Test
    @DisplayName("description contenant une URI javascript: est rejetée")
    void description_avecUriJavascript_rejetee() {
        UpdateActualiteRequest requete = new UpdateActualiteRequest(
                "PROJET", "Titre correct", "javascript:alert(document.cookie)", null, null);

        Set<ConstraintViolation<UpdateActualiteRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }
}
