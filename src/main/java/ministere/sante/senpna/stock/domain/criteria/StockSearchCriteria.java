package ministere.sante.senpna.stock.domain.criteria;

import java.util.UUID;

public record StockSearchCriteria(UUID entrepotId, UUID lotId, UUID medicamentId, Boolean ruptureUniquement,
        Boolean seuilAtteintUniquement) {

    public static StockSearchCriteria vide() {
        return new StockSearchCriteria(null, null, null, null, null);
    }
}
