package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ConfirmerDelaiLivraisonCommand;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.in.ConfirmerDelaiLivraisonUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmerDelaiLivraisonUseCaseImpl implements ConfirmerDelaiLivraisonUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public ConfirmerDelaiLivraisonUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional
    public CommandeAchatDetail confirmer(ConfirmerDelaiLivraisonCommand command) {
        CommandeAchat commande = commandeAchatRepositoryPort.findById(CommandeAchatId.of(command.commandeAchatId()))
                .orElseThrow(CommandeAchatIntrouvableException::new);

        if (!commande.appartientA(FournisseurId.of(command.fournisseurId()))) {
            throw new AccesCommandeAchatRefuseException();
        }

        commande.confirmerDelaiLivraison(command.delaiJours(), command.dateLivraisonConfirmee());

        CommandeAchat saved = commandeAchatRepositoryPort.save(commande);
        return commandeAchatDetailAssembler.assembler(saved);
    }
}
