package ministere.sante.senpna.medicament.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class FamilleId extends EntityId {

    private FamilleId(UUID value) {
        super(value);
    }

    public static FamilleId of(UUID value) {
        return new FamilleId(value);
    }

    public static FamilleId of(String value) {
        return new FamilleId(UUID.fromString(value));
    }

    public static FamilleId generate() {
        return new FamilleId(UUID.randomUUID());
    }
}
