package ministere.sante.senpna.shared.domain.valueobject;

import java.util.Objects;

public final class Quantity {

    private static final String NEGATIVE_QUANTITY_ERROR_MESSAGE = "La quantité à comparer ne peut pas être null ";

    public static final Quantity ZERO = new Quantity(0);

    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    public static Quantity of(int value) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    "La quantité ne peut pas être négative, reçu : " + value);
        }
        return value == 0 ? ZERO : new Quantity(value);
    }

    public int getValue() {
        return value;
    }

    // ── Opérations ────────────────────────────────────────────────────────

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "La quantité à additionner ne peut pas être null");
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        Objects.requireNonNull(other, "La quantité à soustraire ne peut pas être null");
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException(
                    "Une opération arithmétique a produit une quantité négative : " + result
                            + ". Vérifiez l'invariant de stock avant d'appeler subtract().");
        }
        return result == 0 ? ZERO : new Quantity(result);
    }

    public Quantity multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException(
                    "Le facteur de multiplication ne peut pas être négatif : " + factor);
        }
        int result = this.value * factor;
        return result == 0 ? ZERO : new Quantity(result);
    }

    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isPositive() {
        return this.value > 0;
    }

    public boolean isGreaterThan(Quantity other) {
        Objects.requireNonNull(other, NEGATIVE_QUANTITY_ERROR_MESSAGE);
        return this.value > other.value;
    }

    public boolean isGreaterThanOrEqualTo(Quantity other) {
        Objects.requireNonNull(other, NEGATIVE_QUANTITY_ERROR_MESSAGE);
        return this.value >= other.value;
    }

    public boolean isLessThan(Quantity other) {
        Objects.requireNonNull(other, NEGATIVE_QUANTITY_ERROR_MESSAGE);
        return this.value < other.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return this.value == ((Quantity) o).value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}