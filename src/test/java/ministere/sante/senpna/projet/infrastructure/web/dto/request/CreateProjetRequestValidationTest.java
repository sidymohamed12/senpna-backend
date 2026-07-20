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

@DisplayName("CreateProjetRequest — validation Bean Validation (@NoHtml notamment)")
class CreateProjetRequestValidationTest {

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
        CreateProjetRequest requete = new CreateProjetRequest(
                "SANTE_PUBLIQUE", "Programme régional de vaccination", "Description sans balisage.",
                null, null, null);

        assertThat(validator.validate(requete)).isEmpty();
    }

    @Test
    @DisplayName("nom contenant une balise <script> est rejeté")
    void nom_avecScript_rejete() {
        CreateProjetRequest requete = new CreateProjetRequest(
                "SANTE_PUBLIQUE", "<script>alert(1)</script>", "description ok", null, null, null);

        Set<ConstraintViolation<CreateProjetRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("nom");
    }

    @Test
    @DisplayName("description avec gestionnaire d'évènement onerror est rejetée")
    void description_avecOnError_rejetee() {
        CreateProjetRequest requete = new CreateProjetRequest(
                "SANTE_PUBLIQUE", "Nom valide", "<img src=x onerror=alert(1)>", null, null, null);

        Set<ConstraintViolation<CreateProjetRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }
}
