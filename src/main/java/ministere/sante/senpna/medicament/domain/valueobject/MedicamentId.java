package ministere.sante.senpna.medicament.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class MedicamentId extends EntityId {

    private MedicamentId(UUID value) {
        super(value);
    }

    public static MedicamentId of(UUID value) {
        return new MedicamentId(value);
    }

    public static MedicamentId of(String value) {
        return new MedicamentId(UUID.fromString(value));
    }

    public static MedicamentId generate() {
        return new MedicamentId(UUID.randomUUID());
    }
}
