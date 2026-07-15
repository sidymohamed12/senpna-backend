package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.criteria.CommandeAchatSearchCriteria;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.in.ListCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCommandeAchatUseCaseImpl implements ListCommandeAchatUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public ListCommandeAchatUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeAchatPage lister(ListCommandeAchatQuery query) {
        CommandeAchatSearchCriteria criteria = new CommandeAchatSearchCriteria(query.recherche(), query.statut());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<CommandeAchat> result = commandeAchatRepositoryPort.search(criteria, pageRequest);

        return new CommandeAchatPage(
                result.content().stream().map(commandeAchatDetailAssembler::assemblerResume).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
