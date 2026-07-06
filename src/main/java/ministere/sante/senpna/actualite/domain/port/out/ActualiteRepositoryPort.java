package ministere.sante.senpna.actualite.domain.port.out;

import ministere.sante.senpna.actualite.domain.criteria.ActualiteSearchCriteria;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface ActualiteRepositoryPort {

    Optional<Actualite> findById(ActualiteId id);

    Actualite save(Actualite actualite);

    PageResult<Actualite> search(ActualiteSearchCriteria criteria, PageRequest pageRequest);
}
