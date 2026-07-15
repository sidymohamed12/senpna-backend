package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ValiderCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.in.ValiderCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValiderCommandeAchatUseCaseImpl implements ValiderCommandeAchatUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public ValiderCommandeAchatUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional
    public CommandeAchatDetail valider(ValiderCommandeAchatCommand command) {
        CommandeAchat commande = commandeAchatRepositoryPort.findById(CommandeAchatId.of(command.commandeAchatId()))
                .orElseThrow(CommandeAchatIntrouvableException::new);

        commande.validerInterne();

        CommandeAchat saved = commandeAchatRepositoryPort.save(commande);
        return commandeAchatDetailAssembler.assembler(saved);
    }
}
