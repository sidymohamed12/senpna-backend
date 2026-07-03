package ministere.sante.senpna.medicament.domain.port.out;

import ministere.sante.senpna.medicament.domain.criteria.MedicamentSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface MedicamentRepositoryPort {

    Optional<Medicament> findById(MedicamentId id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, MedicamentId id);

    Medicament save(Medicament medicament);

    PageResult<Medicament> search(MedicamentSearchCriteria criteria, PageRequest pageRequest);
}
