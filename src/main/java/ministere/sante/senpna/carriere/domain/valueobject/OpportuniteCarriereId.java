package ministere.sante.senpna.carriere.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class OpportuniteCarriereId extends EntityId {

    private OpportuniteCarriereId(UUID value) {
        super(value);
    }

    public static OpportuniteCarriereId of(UUID value) {
        return new OpportuniteCarriereId(value);
    }

    public static OpportuniteCarriereId of(String value) {
        return new OpportuniteCarriereId(UUID.fromString(value));
    }

    public static OpportuniteCarriereId generate() {
        return new OpportuniteCarriereId(UUID.randomUUID());
    }
}
