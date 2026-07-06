package ministere.sante.senpna.actualite.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class ActualiteId extends EntityId {

    private ActualiteId(UUID value) {
        super(value);
    }

    public static ActualiteId of(UUID value) {
        return new ActualiteId(value);
    }

    public static ActualiteId of(String value) {
        return new ActualiteId(UUID.fromString(value));
    }

    public static ActualiteId generate() {
        return new ActualiteId(UUID.randomUUID());
    }
}
