package ministere.sante.senpna.organisation.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class EntrepotId extends EntityId {

    private EntrepotId(UUID value) {
        super(value);
    }

    public static EntrepotId of(UUID value) {
        return new EntrepotId(value);
    }

    public static EntrepotId of(String value) {
        return new EntrepotId(UUID.fromString(value));
    }

    public static EntrepotId generate() {
        return new EntrepotId(UUID.randomUUID());
    }
}
