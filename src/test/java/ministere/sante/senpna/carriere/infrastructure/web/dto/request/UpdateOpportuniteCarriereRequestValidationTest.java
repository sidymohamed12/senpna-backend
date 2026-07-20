package ministere.sante.senpna.carriere.infrastructure.web.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateOpportuniteCarriereRequest — validation Bean Validation (@NoHtml notamment)")
class UpdateOpportuniteCarriereRequestValidationTest {

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
        UpdateOpportuniteCarriereRequest requete = new UpdateOpportuniteCarriereRequest(
                "Pharmacien-gérant", "Ministère de la Santé", "Description sans balisage.",
                null, "Dakar", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");

        assertThat(validator.validate(requete)).isEmpty();
    }

    @Test
    @DisplayName("titre avec URI javascript: est rejeté")
    void titre_avecUriJavascript_rejete() {
        UpdateOpportuniteCarriereRequest requete = new UpdateOpportuniteCarriereRequest(
                "javascript:alert(1)", "Ministère de la Santé", "Description ok",
                null, "Dakar", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");

        Set<ConstraintViolation<UpdateOpportuniteCarriereRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("titre");
    }

    @Test
    @DisplayName("description avec balise <script> est rejetée")
    void description_avecScript_rejetee() {
        UpdateOpportuniteCarriereRequest requete = new UpdateOpportuniteCarriereRequest(
                "Pharmacien-gérant", "Ministère de la Santé", "<script>evil()</script>",
                null, "Dakar", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");

        Set<ConstraintViolation<UpdateOpportuniteCarriereRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }
}
