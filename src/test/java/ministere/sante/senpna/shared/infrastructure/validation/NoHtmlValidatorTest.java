package ministere.sante.senpna.shared.infrastructure.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NoHtmlValidator — détection de balisage HTML / script dans un texte libre")
class NoHtmlValidatorTest {

    private final NoHtmlValidator validator = new NoHtmlValidator();

    @Test
    @DisplayName("isValid() accepte null (la présence du champ relève de @NotBlank/@NotNull)")
    void isValid_null_accepte() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("isValid() accepte une chaîne vide ou blanche")
    void isValid_vide_accepte() {
        assertThat(validator.isValid("", null)).isTrue();
        assertThat(validator.isValid("   ", null)).isTrue();
    }

    @ParameterizedTest
    @DisplayName("isValid() accepte du texte libre normal, y compris accentué et ponctué")
    @ValueSource(strings = {
            "Campagne nationale de vaccination 2026",
            "Recrutement d'un pharmacien-gérant à Dakar",
            "Prix < 500 F CFA (hors taxe) — livraison 48h", // '<' isolé sans nom de balise ne doit pas déclencher
            "Contactez-nous : contact@senpna.sn",
            "Salaire : 5% de commission sur objectifs"
    })
    void isValid_texteLibreLegitime_accepte(String texte) {
        assertThat(validator.isValid(texte, null)).isTrue();
    }

    @ParameterizedTest
    @DisplayName("isValid() rejette les balises HTML/script")
    @ValueSource(strings = {
            "<script>alert(1)</script>",
            "<img src=x onerror=alert(1)>",
            "<b>gras</b>",
            "<iframe src='https://evil.example'></iframe>",
            "</textarea><script>evil()</script>",
            "<!--injection-->"
    })
    void isValid_baliseHtml_rejette(String texte) {
        assertThat(validator.isValid(texte, null)).isFalse();
    }

    @ParameterizedTest
    @DisplayName("isValid() rejette les gestionnaires d'évènements même sans balise complète")
    @ValueSource(strings = {
            "onclick=alert(1)",
            "onerror = maliciousFn()",
            "ONLOAD=doEvil()"
    })
    void isValid_gestionnaireEvenement_rejette(String texte) {
        assertThat(validator.isValid(texte, null)).isFalse();
    }

    @ParameterizedTest
    @DisplayName("isValid() rejette les URI script (javascript:, vbscript:, data:text/html)")
    @ValueSource(strings = {
            "javascript:alert(1)",
            "JAVASCRIPT:alert(1)",
            "vbscript:msgbox(1)",
            "data:text/html,<script>alert(1)</script>"
    })
    void isValid_uriScript_rejette(String texte) {
        assertThat(validator.isValid(texte, null)).isFalse();
    }
}
