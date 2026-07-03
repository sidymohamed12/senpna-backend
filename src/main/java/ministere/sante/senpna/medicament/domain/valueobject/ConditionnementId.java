package ministere.sante.senpna.medicament.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class ConditionnementId extends EntityId {

    private ConditionnementId(UUID value) {
        super(value);
    }

    public static ConditionnementId of(UUID value) {
        return new ConditionnementId(value);
    }

    public static ConditionnementId of(String value) {
        return new ConditionnementId(UUID.fromString(value));
    }

    public static ConditionnementId generate() {
        return new ConditionnementId(UUID.randomUUID());
    }
}
