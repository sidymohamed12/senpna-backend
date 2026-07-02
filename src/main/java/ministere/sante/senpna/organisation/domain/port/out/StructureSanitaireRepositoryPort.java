package ministere.sante.senpna.organisation.domain.port.out;

import ministere.sante.senpna.organisation.domain.criteria.StructureSanitaireSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface StructureSanitaireRepositoryPort {

    Optional<StructureSanitaire> findById(StructureSanitaireId id);

    boolean existsByCode(String code);

    StructureSanitaire save(StructureSanitaire structureSanitaire);

    PageResult<StructureSanitaire> search(StructureSanitaireSearchCriteria criteria, PageRequest pageRequest);
}
