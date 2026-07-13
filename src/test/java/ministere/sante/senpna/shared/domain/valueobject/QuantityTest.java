package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quantity — value object de quantité entière non négative")
class QuantityTest {

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("valeur positive → construite normalement")
        void valeurPositive() {
            assertThat(Quantity.of(5).getValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("zéro → renvoie la constante partagée ZERO")
        void zero_renvoieConstantePartagee() {
            assertThat(Quantity.of(0)).isSameAs(Quantity.ZERO);
        }

        @Test
        @DisplayName("négative → IllegalArgumentException")
        void negative_leveException() {
            assertThatThrownBy(() -> Quantity.of(-1)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("add() / subtract() / multiply()")
    class Operations {

        @Test
        @DisplayName("add() additionne les valeurs")
        void add_additionne() {
            assertThat(Quantity.of(3).add(Quantity.of(4)).getValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("subtract() soustrait les valeurs")
        void subtract_soustrait() {
            assertThat(Quantity.of(10).subtract(Quantity.of(4)).getValue()).isEqualTo(6);
        }

        @Test
        @DisplayName("subtract() produisant un résultat négatif → IllegalArgumentException")
        void subtract_resultatNegatif_leveException() {
            Quantity quantity = Quantity.of(3);
            Quantity quantityToSubtract = Quantity.of(5);

            assertThatThrownBy(() -> quantity.subtract(quantityToSubtract))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("subtract() résultat exactement zéro → renvoie ZERO")
        void subtract_resultatZero_renvoieZero() {
            assertThat(Quantity.of(5).subtract(Quantity.of(5))).isSameAs(Quantity.ZERO);
        }

        @Test
        @DisplayName("multiply() multiplie par un facteur positif")
        void multiply_facteurPositif() {
            assertThat(Quantity.of(4).multiply(3).getValue()).isEqualTo(12);
        }

        @Test
        @DisplayName("multiply() par un facteur négatif → IllegalArgumentException")
        void multiply_facteurNegatif_leveException() {

            Quantity quantity = Quantity.of(4);
            var quantityToMultiply = -2;

            assertThatThrownBy(() -> quantity.multiply(quantityToMultiply))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("multiply() par zéro → renvoie ZERO")
        void multiply_parZero_renvoieZero() {
            assertThat(Quantity.of(4).multiply(0)).isSameAs(Quantity.ZERO);
        }
    }

    @Nested
    @DisplayName("comparaisons")
    class Comparaisons {

        @Test
        @DisplayName("isZero() / isPositive()")
        void isZeroEtIsPositive() {
            assertThat(Quantity.ZERO.isZero()).isTrue();
            assertThat(Quantity.ZERO.isPositive()).isFalse();
            assertThat(Quantity.of(1).isPositive()).isTrue();
        }

        @Test
        @DisplayName("isGreaterThan() / isGreaterThanOrEqualTo() / isLessThan()")
        void comparaisonsRelatives() {
            assertThat(Quantity.of(5).isGreaterThan(Quantity.of(3))).isTrue();
            assertThat(Quantity.of(5).isGreaterThanOrEqualTo(Quantity.of(5))).isTrue();
            assertThat(Quantity.of(3).isLessThan(Quantity.of(5))).isTrue();
        }
    }

    @Nested
    @DisplayName("equals() / hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("deux quantités de même valeur sont égales")
        void memeValeur_egales() {
            assertThat(Quantity.of(7)).isEqualTo(Quantity.of(7));
            assertThat(Quantity.of(7)).hasSameHashCodeAs(Quantity.of(7));
        }

        @Test
        @DisplayName("valeurs différentes → non égales")
        void valeursDifferentes_nonEgales() {
            assertThat(Quantity.of(7)).isNotEqualTo(Quantity.of(8));
        }
    }
}
