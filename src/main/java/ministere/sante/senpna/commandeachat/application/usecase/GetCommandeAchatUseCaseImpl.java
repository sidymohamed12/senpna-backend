package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.in.GetCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code fournisseurId} de la requête : {@code null} côté PNA (aucune
 * restriction), renseigné côté espace fournisseur (accès restreint au
 * propriétaire).
 */
@Service
public class GetCommandeAchatUseCaseImpl implements GetCommandeAchatUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public GetCommandeAchatUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CommandeAchatDetail obtenir(GetCommandeAchatQuery query) {
        CommandeAchat commande = commandeAchatRepositoryPort.findById(CommandeAchatId.of(query.commandeAchatId()))
                .orElseThrow(CommandeAchatIntrouvableException::new);

        if (query.fournisseurId() != null && !commande.appartientA(FournisseurId.of(query.fournisseurId()))) {
            throw new AccesCommandeAchatRefuseException();
        }

        return commandeAchatDetailAssembler.assembler(commande);
    }
}
