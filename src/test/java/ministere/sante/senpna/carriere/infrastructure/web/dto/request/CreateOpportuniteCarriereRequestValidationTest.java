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

@DisplayName("CreateOpportuniteCarriereRequest — validation Bean Validation (@NoHtml notamment)")
class CreateOpportuniteCarriereRequestValidationTest {

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

    private CreateOpportuniteCarriereRequest requeteValide() {
        return new CreateOpportuniteCarriereRequest(
                "Pharmacien-gérant", "Ministère de la Santé", "Description du poste, sans balisage.",
                null, "Dakar", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");
    }

    @Test
    @DisplayName("une requête entièrement valide ne lève aucune violation")
    void requeteValide_aucuneViolation() {
        assertThat(validator.validate(requeteValide())).isEmpty();
    }

    @Test
    @DisplayName("titre contenant une balise <script> est rejeté")
    void titre_avecScript_rejete() {
        CreateOpportuniteCarriereRequest requete = new CreateOpportuniteCarriereRequest(
                "<script>alert(1)</script>", "Ministère de la Santé", "Description ok",
                null, "Dakar", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");

        Set<ConstraintViolation<CreateOpportuniteCarriereRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("titre");
    }

    @Test
    @DisplayName("nomEntreprise avec gestionnaire d'évènement est rejeté")
    void nomEntreprise_avecOnClick_rejete() {
        CreateOpportuniteCarriereRequest requete = new CreateOpportuniteCarriereRequest(
                "Pharmacien-gérant", "onclick=alert(1)", "Description ok",
                null, "Dakar", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");

        Set<ConstraintViolation<CreateOpportuniteCarriereRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("nomEntreprise");
    }

    @Test
    @DisplayName("description avec balise <iframe> est rejetée")
    void description_avecIframe_rejetee() {
        CreateOpportuniteCarriereRequest requete = new CreateOpportuniteCarriereRequest(
                "Pharmacien-gérant", "Ministère de la Santé", "<iframe src='evil'></iframe>",
                null, "Dakar", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");

        Set<ConstraintViolation<CreateOpportuniteCarriereRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }

    @Test
    @DisplayName("lieu avec balise HTML est rejeté")
    void lieu_avecBaliseHtml_rejete() {
        CreateOpportuniteCarriereRequest requete = new CreateOpportuniteCarriereRequest(
                "Pharmacien-gérant", "Ministère de la Santé", "Description ok",
                null, "<b>Dakar</b>", "CDI", LocalDate.now(), LocalDate.now().plusMonths(1), "rh@senpna.sn");

        Set<ConstraintViolation<CreateOpportuniteCarriereRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("lieu");
    }
}
