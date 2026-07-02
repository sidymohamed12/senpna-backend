package ministere.sante.senpna.organisation.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class RegionId extends EntityId {

    private RegionId(UUID value) {
        super(value);
    }

    public static RegionId of(UUID value) {
        return new RegionId(value);
    }

    public static RegionId of(String value) {
        return new RegionId(UUID.fromString(value));
    }

    public static RegionId generate() {
        return new RegionId(UUID.randomUUID());
    }
}
