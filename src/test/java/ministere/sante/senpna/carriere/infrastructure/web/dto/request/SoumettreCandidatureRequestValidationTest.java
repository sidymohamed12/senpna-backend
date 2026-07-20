package ministere.sante.senpna.carriere.infrastructure.web.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Endpoint public non authentifié ({@code POST /api/candidatures} /
 * {@code POST /api/candidatures/public}) — surface la plus exposée aux
 * tentatives de XSS stocké (contenu relu ensuite par un compte admin en
 * back-office). Ces tests garantissent que {@link
 * ministere.sante.senpna.shared.infrastructure.validation.NoHtml} est bien
 * câblée sur les champs texte libre du formulaire.
 */
@DisplayName("SoumettreCandidatureRequest — validation Bean Validation (@NoHtml notamment)")
class SoumettreCandidatureRequestValidationTest {

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

    private SoumettreCandidatureRequest requeteValide() {
        return new SoumettreCandidatureRequest(
                UUID.randomUUID(),
                "M.",
                "Amadou Diallo",
                "amadou.diallo@example.sn",
                "+221771234567",
                "https://cdn.senpna.sn/cv/abc.pdf",
                null,
                "Motivé par ce poste.",
                true);
    }

    @Test
    @DisplayName("une requête entièrement valide ne lève aucune violation")
    void requeteValide_aucuneViolation() {
        assertThat(validator.validate(requeteValide())).isEmpty();
    }

    @Test
    @DisplayName("nomComplet contenant une balise <script> est rejeté")
    void nomComplet_avecScript_rejete() {
        SoumettreCandidatureRequest requete = new SoumettreCandidatureRequest(
                UUID.randomUUID(), "M.", "<script>alert(1)</script>",
                "a@b.sn", "+221771234567", "https://cdn.senpna.sn/cv/abc.pdf",
                null, "ok", true);

        Set<ConstraintViolation<SoumettreCandidatureRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("nomComplet");
    }

    @Test
    @DisplayName("messageComplementaire avec gestionnaire d'évènement onerror est rejeté")
    void messageComplementaire_avecOnError_rejete() {
        SoumettreCandidatureRequest requete = new SoumettreCandidatureRequest(
                UUID.randomUUID(), "M.", "Amadou Diallo",
                "a@b.sn", "+221771234567", "https://cdn.senpna.sn/cv/abc.pdf",
                null, "<img src=x onerror=alert(1)>", true);

        Set<ConstraintViolation<SoumettreCandidatureRequest>> violations = validator.validate(requete);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("messageComplementaire");
    }

    @Test
    @DisplayName("civilite avec URI javascript: est rejetée")
    void civilite_avecUriJavascript_rejetee() {
        SoumettreCandidatureRequest requete = new SoumettreCandidatureRequest(
                UUID.randomUUID(), "javascript:alert(1)", "Amadou Diallo",
                "a@b.sn", "+221771234567", "https://cdn.senpna.sn/cv/abc.pdf",
                null, "ok", true);

        assertThat(validator.validate(requete))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("civilite");
    }

    @Test
    @DisplayName("consentementRgpd=false est rejeté (obligatoire pour postuler)")
    void consentementRgpd_absent_rejete() {
        SoumettreCandidatureRequest requete = new SoumettreCandidatureRequest(
                UUID.randomUUID(), "M.", "Amadou Diallo",
                "a@b.sn", "+221771234567", "https://cdn.senpna.sn/cv/abc.pdf",
                null, "ok", false);

        assertThat(validator.validate(requete))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("consentementRgpd");
    }

    @Test
    @DisplayName("messageComplementaire vide reste valide (@NoHtml n'exige pas de contenu)")
    void messageComplementaire_vide_valide() {
        SoumettreCandidatureRequest requete = new SoumettreCandidatureRequest(
                UUID.randomUUID(), "M.", "Amadou Diallo",
                "a@b.sn", "+221771234567", "https://cdn.senpna.sn/cv/abc.pdf",
                null, null, true);

        assertThat(validator.validate(requete)).isEmpty();
    }
}
