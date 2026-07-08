package ministere.sante.senpna.carriere.domain.port.out;

import ministere.sante.senpna.carriere.domain.criteria.CandidatureSearchCriteria;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.valueobject.CandidatureId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface CandidatureRepositoryPort {

    Optional<Candidature> findById(CandidatureId id);

    Candidature save(Candidature candidature);

    PageResult<Candidature> search(CandidatureSearchCriteria criteria, PageRequest pageRequest);
}
