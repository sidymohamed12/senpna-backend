package ministere.sante.senpna.projet.infrastructure.web.dto.request;

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

@DisplayName("UpdateProjetRequest — validation Bean Validation (@NoHtml notamment)")
class UpdateProjetRequestValidationTest {

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
        UpdateProjetRequest requete = new UpdateProjetRequest(
                "SANTE_PUBLIQUE", "Programme mis à jour", "Description mise à jour.", null, null, null);

        assertThat(validator.validate(requete)).isEmpty();
    }

    @Test
    @DisplayName("nom avec balise HTML est rejeté")
    void nom_avecBaliseHtml_rejete() {
        UpdateProjetRequest requete = new UpdateProjetRequest(
                "SANTE_PUBLIQUE", "<b>Programme</b>", "description ok", null, null, null);

        Set<ConstraintViolation<UpdateProjetRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("nom");
    }

    @Test
    @DisplayName("description avec URI javascript: est rejetée")
    void description_avecUriJavascript_rejetee() {
        UpdateProjetRequest requete = new UpdateProjetRequest(
                "SANTE_PUBLIQUE", "Nom valide", "javascript:alert(document.cookie)", null, null, null);

        Set<ConstraintViolation<UpdateProjetRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }
}
