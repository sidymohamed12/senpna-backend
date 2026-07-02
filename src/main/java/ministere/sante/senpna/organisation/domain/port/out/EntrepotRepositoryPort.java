package ministere.sante.senpna.organisation.domain.port.out;

import ministere.sante.senpna.organisation.domain.criteria.EntrepotSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface EntrepotRepositoryPort {

    Optional<Entrepot> findById(EntrepotId id);

    boolean existsByCode(String code);

    Entrepot save(Entrepot entrepot);

    PageResult<Entrepot> search(EntrepotSearchCriteria criteria, PageRequest pageRequest);
}
