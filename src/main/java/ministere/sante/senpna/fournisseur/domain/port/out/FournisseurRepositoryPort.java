package ministere.sante.senpna.fournisseur.domain.port.out;

import ministere.sante.senpna.fournisseur.domain.criteria.FournisseurSearchCriteria;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface FournisseurRepositoryPort {

    Optional<Fournisseur> findById(FournisseurId id);

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, FournisseurId id);

    Fournisseur save(Fournisseur fournisseur);

    PageResult<Fournisseur> search(FournisseurSearchCriteria criteria, PageRequest pageRequest);
}
