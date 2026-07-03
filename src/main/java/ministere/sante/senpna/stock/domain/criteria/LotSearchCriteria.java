package ministere.sante.senpna.stock.domain.criteria;

import ministere.sante.senpna.stock.domain.valueobject.StatutLot;

import java.util.UUID;

public record LotSearchCriteria(String recherche, UUID medicamentId, UUID fournisseurId, StatutLot statut) {

    public static LotSearchCriteria vide() {
        return new LotSearchCriteria(null, null, null, null);
    }
}
