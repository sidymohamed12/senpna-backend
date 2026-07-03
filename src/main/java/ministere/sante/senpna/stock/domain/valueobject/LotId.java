package ministere.sante.senpna.stock.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class LotId extends EntityId {

    private LotId(UUID value) {
        super(value);
    }

    public static LotId of(UUID value) {
        return new LotId(value);
    }

    public static LotId of(String value) {
        return new LotId(UUID.fromString(value));
    }

    public static LotId generate() {
        return new LotId(UUID.randomUUID());
    }
}
