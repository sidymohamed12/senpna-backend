package ministere.sante.senpna.stock.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projection Spring Data (interface-based) du résultat brut de la requête
 * d'agrégation catalogue — détail d'infrastructure exclusivement : jamais
 * exposée au-delà de {@link CatalogueStockJpaRepository} /
 * {@code CatalogueStockQueryAdapter}, qui la convertit en
 * {@link ministere.sante.senpna.shared.domain.projection.StockAgregeProjection}
 * (domaine).
 */
public interface CatalogueAggregatRow {

    UUID getEntrepotId();

    UUID getMedicamentId();

    BigDecimal getQuantiteDisponible();

    BigDecimal getQuantiteReservee();

    Long getNombreLotsActifs();

    LocalDate getProchaineDateExpiration();

    BigDecimal getPrixVenteMoyen();

    UUID getFournisseurId();
}
