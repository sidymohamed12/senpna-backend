package ministere.sante.senpna.stock.domain.port.out;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StockId;

import java.util.Optional;

public interface StockRepositoryPort {

    Optional<Stock> findById(StockId id);

    Optional<Stock> findByEntrepotIdAndLotId(EntrepotId entrepotId, LotId lotId);

    /**
     * Verrouille pessimistement la ligne de stock (SELECT ... FOR UPDATE)
     * pour le couple (entrepôt, lot) — utilisé lors des opérations
     * concurrentes (réservation, entrée, sortie) afin d'éviter les
     * conditions de course sur les quantités.
     */
    Optional<Stock> findByEntrepotIdAndLotIdForUpdate(EntrepotId entrepotId, LotId lotId);

    Stock save(Stock stock);

    PageResult<Stock> search(StockSearchCriteria criteria, PageRequest pageRequest);
}
