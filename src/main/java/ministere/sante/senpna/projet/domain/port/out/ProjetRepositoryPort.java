package ministere.sante.senpna.projet.domain.port.out;

import ministere.sante.senpna.projet.domain.criteria.ProjetSearchCriteria;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface ProjetRepositoryPort {

    Optional<Projet> findById(ProjetId id);

    Projet save(Projet projet);

    PageResult<Projet> search(ProjetSearchCriteria criteria, PageRequest pageRequest);
}
