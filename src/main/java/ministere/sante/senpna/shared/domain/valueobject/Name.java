package ministere.sante.senpna.shared.domain.valueobject;

import java.util.Objects;

public abstract class Name {

    private final String value;

    protected Name(String value) {
        Objects.requireNonNull(value, "La valeur ne peut pas être null");
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(getFieldLabel() + " ne peut pas être vide");
        }
        if (trimmed.length() < getMinLength()) {
            throw new IllegalArgumentException(
                    getFieldLabel() + " doit contenir au moins " + getMinLength() + " caractères");
        }
        if (trimmed.length() > getMaxLength()) {
            throw new IllegalArgumentException(
                    getFieldLabel() + " ne peut pas dépasser " + getMaxLength() + " caractères");
        }
        validateAdditional(trimmed);
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    protected int getMinLength() {
        return 2;
    }

    protected int getMaxLength() {
        return 100;
    }

    protected String getFieldLabel() {
        return "La valeur";
    }

    protected void validateAdditional(String value) {
        // aucune validation additionnelle par défaut
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Name that = (Name) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public String toString() {
        return value;
    }
}