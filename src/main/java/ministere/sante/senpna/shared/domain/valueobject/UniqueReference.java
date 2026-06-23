package ministere.sante.senpna.shared.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public abstract class UniqueReference {

    private final String value;

    protected UniqueReference(String value) {
        Objects.requireNonNull(value, "La référence ne peut pas être null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("La référence ne peut pas être vide");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Génère une référence unique avec le préfixe donné.
     * Format : {PREFIX}-{12 caractères hex uppercase}
     * Exemple : CMD-A1B2C3D4E5F6
     *
     * @param prefix préfixe métier (ex: "CMD", "PAY") — non null, non vide
     */
    protected static String genererValeur(String prefix) {
        Objects.requireNonNull(prefix, "Le préfixe ne peut pas être null");
        if (prefix.isBlank()) {
            throw new IllegalArgumentException("Le préfixe ne peut pas être vide");
        }
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return prefix + "-" + uuid.substring(0, 12);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UniqueReference that = (UniqueReference) o;
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