package ministere.sante.senpna.stock.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class MouvementStockId extends EntityId {

    private MouvementStockId(UUID value) {
        super(value);
    }

    public static MouvementStockId of(UUID value) {
        return new MouvementStockId(value);
    }

    public static MouvementStockId of(String value) {
        return new MouvementStockId(UUID.fromString(value));
    }

    public static MouvementStockId generate() {
        return new MouvementStockId(UUID.randomUUID());
    }
}
