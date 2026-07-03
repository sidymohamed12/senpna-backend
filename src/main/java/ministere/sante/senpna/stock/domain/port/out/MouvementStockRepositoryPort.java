package ministere.sante.senpna.stock.domain.port.out;

import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.MouvementSearchCriteria;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.valueobject.MouvementStockId;

import java.util.Optional;

public interface MouvementStockRepositoryPort {

    Optional<MouvementStock> findById(MouvementStockId id);

    /** Le journal des mouvements est un append-only log : pas de mise à jour, seulement des insertions. */
    MouvementStock save(MouvementStock mouvementStock);

    PageResult<MouvementStock> search(MouvementSearchCriteria criteria, PageRequest pageRequest);
}
