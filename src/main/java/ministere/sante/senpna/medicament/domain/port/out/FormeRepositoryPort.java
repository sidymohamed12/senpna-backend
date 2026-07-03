package ministere.sante.senpna.medicament.domain.port.out;

import ministere.sante.senpna.medicament.domain.criteria.FormeSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface FormeRepositoryPort {

    Optional<Forme> findById(FormeId id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, FormeId id);

    Forme save(Forme forme);

    PageResult<Forme> search(FormeSearchCriteria criteria, PageRequest pageRequest);
}
