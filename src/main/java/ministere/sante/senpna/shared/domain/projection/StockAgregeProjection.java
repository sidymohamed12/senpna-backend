package ministere.sante.senpna.shared.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ligne de stock agrégée par (entrepôt, médicament) — <strong>projection
 * de lecture, jamais une table</strong>. Calculée à la volée par le
 * module {@code stock} (seul propriétaire des tables {@code stocks} et
 * {@code lots}) en additionnant les lignes {@code Stock} des lots
 * {@code ACTIF} non expirés d'un même médicament dans un même entrepôt.
 *
 * <p>
 * Exposée dans {@code shared} — au même titre que {@link EntrepotProjection}
 * ou {@link RegionProjection} — pour permettre à d'autres features (ex :
 * {@code catalogue}) de consommer cette agrégation sans jamais dépendre
 * du module {@code stock} directement.
 * </p>
 *
 * @param entrepotId              entrepôt (PNA centrale ou PRA) sur lequel
 *                                porte l'agrégation
 * @param medicamentId            médicament agrégé
 * @param quantiteDisponible      somme des quantités disponibles des lignes de
 *                                stock concernées
 * @param quantiteReservee        somme des quantités déjà réservées
 * @param nombreLotsActifs        nombre de lots {@code ACTIF} non expirés
 *                                contribuant à l'agrégation
 * @param prochaineDateExpiration date d'expiration la plus proche parmi les
 *                                lots contribuants (repère FEFO)
 * @param prixVenteMoyen          prix de vente moyen indicatif des lots
 *                                contribuants (peut être {@code null})
 * @param fournisseurId           fournisseur du lot le plus proche de la
 *                                péremption (FEFO) — celui qui sera
 *                                effectivement sorti en premier ; sert de
 *                                repère d'affichage, un médicament peut
 *                                avoir été réapprovisionné auprès de plusieurs
 *                                fournisseurs au fil du temps
 */
public record StockAgregeProjection(UUID entrepotId, UUID medicamentId, BigDecimal quantiteDisponible,
        BigDecimal quantiteReservee, int nombreLotsActifs, LocalDate prochaineDateExpiration,
        BigDecimal prixVenteMoyen, UUID fournisseurId) {

    public BigDecimal quantiteDisponibleALaVente() {
        return quantiteDisponible.subtract(quantiteReservee);
    }

    public boolean enRupture() {
        return quantiteDisponibleALaVente().signum() <= 0;
    }
}
