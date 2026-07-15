package ministere.sante.senpna.commandeachat.domain.port.out;

import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface FactureRepositoryPort {

    Optional<Facture> findById(FactureId id);

    Facture save(Facture facture);

    PageResult<Facture> findAll(StatutFacture statut, PageRequest pageRequest);

    PageResult<Facture> findByFournisseurId(FournisseurId fournisseurId, StatutFacture statut,
            PageRequest pageRequest);
}
