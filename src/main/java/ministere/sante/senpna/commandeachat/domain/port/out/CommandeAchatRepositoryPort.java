package ministere.sante.senpna.commandeachat.domain.port.out;

import ministere.sante.senpna.commandeachat.domain.criteria.CommandeAchatSearchCriteria;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface CommandeAchatRepositoryPort {

    Optional<CommandeAchat> findById(CommandeAchatId id);

    boolean existsByReferenceIgnoreCase(String reference);

    CommandeAchat save(CommandeAchat commandeAchat);

    PageResult<CommandeAchat> search(CommandeAchatSearchCriteria criteria, PageRequest pageRequest);

    PageResult<CommandeAchat> findByFournisseurId(FournisseurId fournisseurId, StatutCommandeAchat statut,
            PageRequest pageRequest);
}
