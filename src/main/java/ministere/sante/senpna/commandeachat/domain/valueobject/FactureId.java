package ministere.sante.senpna.commandeachat.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class FactureId extends EntityId {

    private FactureId(UUID value) {
        super(value);
    }

    public static FactureId of(UUID value) {
        return new FactureId(value);
    }

    public static FactureId of(String value) {
        return new FactureId(UUID.fromString(value));
    }

    public static FactureId generate() {
        return new FactureId(UUID.randomUUID());
    }
}
