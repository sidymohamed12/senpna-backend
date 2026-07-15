package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListMesCommandesQuery;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.in.ListMesCommandesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListMesCommandesUseCaseImpl implements ListMesCommandesUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public ListMesCommandesUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeAchatPage lister(ListMesCommandesQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), null, null);

        PageResult<CommandeAchat> result = commandeAchatRepositoryPort.findByFournisseurId(
                FournisseurId.of(query.fournisseurId()), query.statut(), pageRequest);

        return new CommandeAchatPage(
                result.content().stream().map(commandeAchatDetailAssembler::assemblerResume).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
