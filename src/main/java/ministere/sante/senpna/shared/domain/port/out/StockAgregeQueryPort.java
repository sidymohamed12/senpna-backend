package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interroge, en lecture seule, l'agrégation de stock par (entrepôt,
 * médicament) — <strong>ne persiste rien</strong>, aucune table
 * n'existe pour cette vue : elle est recalculée à la demande à partir des
 * lignes {@code Stock}/{@code Lot} déjà possédées par le module
 * {@code stock}, seul implémenteur de ce port.
 *
 * <p>
 * Défini dans {@code shared} — comme {@link EntrepotQueryPort} ou
 * {@link RegionQueryPort} — pour que n'importe quelle feature (notamment
 * {@code catalogue}) puisse consommer cette agrégation sans dépendre
 * directement du module {@code stock}.
 * </p>
 */
public interface StockAgregeQueryPort {

    /** Agrège, par médicament, les lignes de stock actives d'un unique entrepôt. */
    List<StockAgregeProjection> rechercherParEntrepot(UUID entrepotId);

    /**
     * Agrège, par couple (entrepôt, médicament), les lignes de stock
     * actives de plusieurs entrepôts — utile pour ventiler une
     * disponibilité entrepôt par entrepôt plutôt que de la sommer.
     */
    List<StockAgregeProjection> rechercherParEntrepots(Set<UUID> entrepotIds);
}
