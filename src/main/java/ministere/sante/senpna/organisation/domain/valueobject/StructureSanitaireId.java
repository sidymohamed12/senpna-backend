package ministere.sante.senpna.organisation.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class StructureSanitaireId extends EntityId {

    private StructureSanitaireId(UUID value) {
        super(value);
    }

    public static StructureSanitaireId of(UUID value) {
        return new StructureSanitaireId(value);
    }

    public static StructureSanitaireId of(String value) {
        return new StructureSanitaireId(UUID.fromString(value));
    }

    public static StructureSanitaireId generate() {
        return new StructureSanitaireId(UUID.randomUUID());
    }
}
