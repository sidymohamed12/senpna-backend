package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.RejeterCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.in.RejeterCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejeterCommandeAchatUseCaseImpl implements RejeterCommandeAchatUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public RejeterCommandeAchatUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional
    public CommandeAchatDetail rejeter(RejeterCommandeAchatCommand command) {
        CommandeAchat commande = commandeAchatRepositoryPort.findById(CommandeAchatId.of(command.commandeAchatId()))
                .orElseThrow(CommandeAchatIntrouvableException::new);

        commande.rejeter(command.motif());

        CommandeAchat saved = commandeAchatRepositoryPort.save(commande);
        return commandeAchatDetailAssembler.assembler(saved);
    }
}
