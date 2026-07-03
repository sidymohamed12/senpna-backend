package ministere.sante.senpna.stock.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class StockId extends EntityId {

    private StockId(UUID value) {
        super(value);
    }

    public static StockId of(UUID value) {
        return new StockId(value);
    }

    public static StockId of(String value) {
        return new StockId(UUID.fromString(value));
    }

    public static StockId generate() {
        return new StockId(UUID.randomUUID());
    }
}
