package ministere.sante.senpna.shared.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public abstract class EntityId {

    private final UUID value;

    protected EntityId(UUID value) {
        Objects.requireNonNull(value, "L'identifiant ne peut pas être null");
        this.value = value;
    }

    protected EntityId(String value) {
        Objects.requireNonNull(value, "L'identifiant ne peut pas être null");
        this.value = UUID.fromString(value); // lève IllegalArgumentException si malformé
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EntityId that = (EntityId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}