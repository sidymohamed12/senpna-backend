package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UniqueReference — base commune des références métier uniques")
class UniqueReferenceTest {

    private static final class TestReference extends UniqueReference {
        TestReference(String value) {
            super(value);
        }

        static TestReference generer(String prefix) {
            return new TestReference(genererValeur(prefix));
        }
    }

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("valeur valide → construite normalement")
        void valeurValide() {
            assertThat(new TestReference("CMD-ABC123").getValue()).isEqualTo("CMD-ABC123");
        }

        @Test
        @DisplayName("valeur null → NullPointerException")
        void valeurNull_leveException() {
            assertThatThrownBy(() -> new TestReference(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("valeur vide/blanche → IllegalArgumentException")
        void valeurVide_leveException() {
            assertThatThrownBy(() -> new TestReference("   ")).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("genererValeur()")
    class GenererValeur {

        @Test
        @DisplayName("génère une valeur au format PREFIX-12CARACTERES")
        void formatPrefixe() {
            TestReference reference = TestReference.generer("CMD");

            assertThat(reference.getValue()).matches("^CMD-[0-9A-F]{12}$");
        }

        @Test
        @DisplayName("préfixe null → NullPointerException")
        void prefixeNull_leveException() {
            assertThatThrownBy(() -> TestReference.generer(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("génère des valeurs différentes à chaque appel")
        void genereValeursDifferentes() {
            assertThat(TestReference.generer("CMD").getValue())
                    .isNotEqualTo(TestReference.generer("CMD").getValue());
        }
    }

    @Nested
    @DisplayName("equals() / hashCode() / toString()")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("même valeur → égales")
        void memeValeur_egales() {
            assertThat(new TestReference("CMD-A")).isEqualTo(new TestReference("CMD-A"));
        }

        @Test
        @DisplayName("valeurs différentes → non égales")
        void valeursDifferentes_nonEgales() {
            assertThat(new TestReference("CMD-A")).isNotEqualTo(new TestReference("CMD-B"));
        }

        @Test
        @DisplayName("toString() renvoie la valeur brute")
        void toString_renvoieValeurBrute() {
            assertThat(new TestReference("CMD-A")).hasToString("CMD-A");
        }
    }
}
