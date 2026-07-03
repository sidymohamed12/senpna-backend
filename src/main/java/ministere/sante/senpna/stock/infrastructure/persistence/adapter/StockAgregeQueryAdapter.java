package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.shared.domain.port.out.StockAgregeQueryPort;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.CatalogueAggregatRow;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.CatalogueStockJpaRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implémentation du port {@code shared} {@link StockAgregeQueryPort} — le
 * module {@code stock} reste le seul propriétaire des tables
 * {@code stocks}/{@code lots} ; les autres features (notamment
 * {@code catalogue}) n'accèdent qu'à cette projection agrégée en lecture,
 * jamais aux entités JPA {@code StockJpaEntity}/{@code LotJpaEntity}
 * elles-mêmes. Aucune table {@code catalogue} n'est créée : l'agrégation
 * est recalculée à chaque appel par {@link CatalogueStockJpaRepository}.
 */
@Component
@Transactional(readOnly = true)
public class StockAgregeQueryAdapter implements StockAgregeQueryPort {

    private final CatalogueStockJpaRepository catalogueStockJpaRepository;

    public StockAgregeQueryAdapter(CatalogueStockJpaRepository catalogueStockJpaRepository) {
        this.catalogueStockJpaRepository = catalogueStockJpaRepository;
    }

    @Override
    public List<StockAgregeProjection> rechercherParEntrepot(UUID entrepotId) {
        return rechercherParEntrepots(Set.of(entrepotId));
    }

    @Override
    public List<StockAgregeProjection> rechercherParEntrepots(Set<UUID> entrepotIds) {
        if (entrepotIds == null || entrepotIds.isEmpty()) {
            return List.of();
        }
        return catalogueStockJpaRepository.agregerParEntrepots(entrepotIds).stream()
                .map(this::toProjection)
                .toList();
    }

    private StockAgregeProjection toProjection(CatalogueAggregatRow row) {
        return new StockAgregeProjection(
                row.getEntrepotId(),
                row.getMedicamentId(),
                row.getQuantiteDisponible(),
                row.getQuantiteReservee(),
                row.getNombreLotsActifs() != null ? row.getNombreLotsActifs().intValue() : 0,
                row.getProchaineDateExpiration(),
                row.getPrixVenteMoyen());
    }
}
