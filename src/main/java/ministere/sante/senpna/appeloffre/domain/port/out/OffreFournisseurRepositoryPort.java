package ministere.sante.senpna.appeloffre.domain.port.out;

import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface OffreFournisseurRepositoryPort {

    Optional<OffreFournisseur> findById(OffreFournisseurId id);

    boolean existsByAppelOffreIdAndFournisseurIdAndStatut(AppelOffreId appelOffreId, FournisseurId fournisseurId,
            StatutOffre statut);

    OffreFournisseur save(OffreFournisseur offre);

    PageResult<OffreFournisseur> findByAppelOffreId(AppelOffreId appelOffreId, PageRequest pageRequest);

    PageResult<OffreFournisseur> findByFournisseurId(FournisseurId fournisseurId, StatutOffre statut,
            PageRequest pageRequest);
}
