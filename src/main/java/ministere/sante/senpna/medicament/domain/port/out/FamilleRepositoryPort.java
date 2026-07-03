package ministere.sante.senpna.medicament.domain.port.out;

import ministere.sante.senpna.medicament.domain.criteria.FamilleSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface FamilleRepositoryPort {

    Optional<Famille> findById(FamilleId id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, FamilleId id);

    Famille save(Famille famille);

    PageResult<Famille> search(FamilleSearchCriteria criteria, PageRequest pageRequest);
}
