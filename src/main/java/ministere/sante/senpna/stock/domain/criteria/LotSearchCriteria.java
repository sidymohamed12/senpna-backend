package ministere.sante.senpna.stock.domain.criteria;

import ministere.sante.senpna.stock.domain.valueobject.StatutLot;

import java.util.UUID;

/**
 * @param entrepotId ne remonte que les lots ayant au moins une ligne de
 *                   stock dans cet entrepôt — utilisé pour le scoping
 *                   organisationnel (cf. {@code EntrepotScopeGuard}) :
 *                   forcé au propre entrepôt d'un acteur PRA, laissé
 *                   libre (y compris {@code null}) pour un acteur PNA.
 */
public record LotSearchCriteria(String recherche, UUID medicamentId, UUID fournisseurId, StatutLot statut,
        UUID entrepotId) {

    public static LotSearchCriteria vide() {
        return new LotSearchCriteria(null, null, null, null, null);
    }
}
