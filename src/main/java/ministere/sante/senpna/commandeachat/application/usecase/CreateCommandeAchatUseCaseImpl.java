package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CreateCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.LigneCommandeAchatInput;
import ministere.sante.senpna.commandeachat.domain.exception.ReferenceCommandeAchatDejaUtiliseeException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.in.CreateCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Création d'un bon de commande d'achat fournisseur par la PNA (cf. doc.
 * métier §d « Création du Bon de Commande »).
 */
@Service
public class CreateCommandeAchatUseCaseImpl implements CreateCommandeAchatUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public CreateCommandeAchatUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional
    public CommandeAchatDetail creer(CreateCommandeAchatCommand command) {
        if (commandeAchatRepositoryPort.existsByReferenceIgnoreCase(command.reference().trim())) {
            throw new ReferenceCommandeAchatDejaUtiliseeException(command.reference());
        }

        List<LigneCommandeAchat> lignes = command.lignes().stream().map(this::toLigne).toList();

        CommandeAchat commande = CommandeAchat.creer(new CommandeAchat.CreationCommand(command.reference(), FournisseurId.of(command.fournisseurId()),
                EntrepotId.of(command.entrepotDestinationId()), lignes, command.commentaire()));

        CommandeAchat saved = commandeAchatRepositoryPort.save(commande);
        return commandeAchatDetailAssembler.assembler(saved);
    }

    private LigneCommandeAchat toLigne(LigneCommandeAchatInput input) {
        return LigneCommandeAchat.creer(new LigneCommandeAchat.CreationCommand(MedicamentId.of(input.medicamentId()),
                ConditionnementId.of(input.conditionnementId()), input.quantiteCommandee(), input.prixUnitaire()));
    }
}
