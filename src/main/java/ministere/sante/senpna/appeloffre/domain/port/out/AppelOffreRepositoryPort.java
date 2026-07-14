package ministere.sante.senpna.appeloffre.domain.port.out;

import ministere.sante.senpna.appeloffre.domain.criteria.AppelOffreSearchCriteria;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface AppelOffreRepositoryPort {

    Optional<AppelOffre> findById(AppelOffreId id);

    boolean existsByReferenceIgnoreCase(String reference);

    AppelOffre save(AppelOffre appelOffre);

    PageResult<AppelOffre> search(AppelOffreSearchCriteria criteria, PageRequest pageRequest);

    /**
     * Appels d'offres publiés dont la date de clôture est dépassée —
     * utilisé par le scheduler de clôture automatique.
     */
    java.util.List<AppelOffre> findPubliesAvecClotureDepassee();
}
