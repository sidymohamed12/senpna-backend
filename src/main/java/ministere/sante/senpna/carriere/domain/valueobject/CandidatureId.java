package ministere.sante.senpna.carriere.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class CandidatureId extends EntityId {

    private CandidatureId(UUID value) {
        super(value);
    }

    public static CandidatureId of(UUID value) {
        return new CandidatureId(value);
    }

    public static CandidatureId of(String value) {
        return new CandidatureId(UUID.fromString(value));
    }

    public static CandidatureId generate() {
        return new CandidatureId(UUID.randomUUID());
    }
}
