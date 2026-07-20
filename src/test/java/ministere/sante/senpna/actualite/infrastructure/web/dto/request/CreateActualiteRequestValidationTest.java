package ministere.sante.senpna.actualite.infrastructure.web.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateActualiteRequest — validation Bean Validation (@NoHtml notamment)")
class CreateActualiteRequestValidationTest {

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
        CreateActualiteRequest requete = new CreateActualiteRequest(
                "PROJET", "Lancement du programme régional", "Une description sans balisage.",
                null, List.of("santé", "sénégal"));

        assertThat(validator.validate(requete)).isEmpty();
    }

    @Test
    @DisplayName("titre contenant une balise <script> est rejeté")
    void titre_avecScript_rejete() {
        CreateActualiteRequest requete = new CreateActualiteRequest(
                "PROJET", "<script>alert(1)</script>", "description ok", null, null);

        Set<ConstraintViolation<CreateActualiteRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("titre");
    }

    @Test
    @DisplayName("description contenant un gestionnaire d'évènement onerror est rejetée")
    void description_avecOnError_rejetee() {
        CreateActualiteRequest requete = new CreateActualiteRequest(
                "PROJET", "Titre correct", "<img src=x onerror=alert(1)>", null, null);

        Set<ConstraintViolation<CreateActualiteRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }

    @Test
    @DisplayName("titre manquant reste rejeté par @NotBlank indépendamment de @NoHtml")
    void titre_manquant_rejete() {
        CreateActualiteRequest requete = new CreateActualiteRequest(
                "PROJET", "", "description ok", null, null);

        assertThat(validator.validate(requete))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("titre");
    }

    @Test
    @DisplayName("description absente (null) reste valide — champ optionnel")
    void description_absente_valide() {
        CreateActualiteRequest requete = new CreateActualiteRequest(
                "PROJET", "Titre valide", null, null, null);

        assertThat(validator.validate(requete)).isEmpty();
    }
}
