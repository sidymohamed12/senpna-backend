package ministere.sante.senpna.medicament.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class FormeId extends EntityId {

    private FormeId(UUID value) {
        super(value);
    }

    public static FormeId of(UUID value) {
        return new FormeId(value);
    }

    public static FormeId of(String value) {
        return new FormeId(UUID.fromString(value));
    }

    public static FormeId generate() {
        return new FormeId(UUID.randomUUID());
    }
}
