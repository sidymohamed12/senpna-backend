package ministere.sante.senpna.shared.infrastructure.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LikePatternEscaper — échappement des métacaractères LIKE (%, _, \\)")
class LikePatternEscaperTest {

    @Test
    @DisplayName("escape() retourne null quand l'entrée est null")
    void escape_null_retourneNull() {
        assertThat(LikePatternEscaper.escape(null)).isNull();
    }

    @Test
    @DisplayName("escape() ne modifie pas un texte sans métacaractère")
    void escape_texteSimple_inchange() {
        assertThat(LikePatternEscaper.escape("paracetamol")).isEqualTo("paracetamol");
    }

    @Test
    @DisplayName("escape() échappe le caractère % (wildcard multi-caractères)")
    void escape_pourcent_echappe() {
        assertThat(LikePatternEscaper.escape("100%"))
                .isEqualTo("100\\%");
    }

    @Test
    @DisplayName("escape() échappe le caractère _ (wildcard mono-caractère)")
    void escape_underscore_echappe() {
        assertThat(LikePatternEscaper.escape("lot_a"))
                .isEqualTo("lot\\_a");
    }

    @Test
    @DisplayName("escape() échappe le backslash lui-même avant les autres caractères")
    void escape_backslash_echappe() {
        assertThat(LikePatternEscaper.escape("a\\b"))
                .isEqualTo("a\\\\b");
    }

    @Test
    @DisplayName("escape() gère une combinaison de tous les métacaractères")
    void escape_combinaison() {
        assertThat(LikePatternEscaper.escape("100%_off\\now"))
                .isEqualTo("100\\%\\_off\\\\now");
    }

    @Test
    @DisplayName("escape() neutralise une tentative de recherche joker pur (contournement de filtre)")
    void escape_wildcardSeul_neutralise() {
        // Sans échappement, "%" recherché tel quel deviendrait le motif LIKE '%%%'
        // qui matche absolument tout le référentiel — l'échappement doit
        // le transformer en une recherche littérale du caractère '%'.
        assertThat(LikePatternEscaper.escape("%")).isEqualTo("\\%");
    }

    @Test
    @DisplayName("escape() sur chaîne vide retourne une chaîne vide")
    void escape_chaineVide() {
        assertThat(LikePatternEscaper.escape("")).isEmpty();
    }

    @Test
    @DisplayName("escapeChar() retourne le backslash comme caractère d'échappement")
    void escapeChar_retourneBackslash() {
        assertThat(LikePatternEscaper.escapeChar()).isEqualTo('\\');
    }
}
