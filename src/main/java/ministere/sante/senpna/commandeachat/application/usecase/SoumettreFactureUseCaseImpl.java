package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.SoumettreFactureCommand;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.in.SoumettreFactureUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dépôt d'une facture par le fournisseur pour une commande d'achat (cf.
 * doc. métier « Soumission de Factures »). La commande référencée doit
 * appartenir au fournisseur — une facture ne peut jamais être déposée
 * pour la commande d'un autre fournisseur.
 */
@Service
public class SoumettreFactureUseCaseImpl implements SoumettreFactureUseCase {

    private final FactureRepositoryPort factureRepositoryPort;
    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final FactureDetailAssembler factureDetailAssembler;

    public SoumettreFactureUseCaseImpl(FactureRepositoryPort factureRepositoryPort,
            CommandeAchatRepositoryPort commandeAchatRepositoryPort, FactureDetailAssembler factureDetailAssembler) {
        this.factureRepositoryPort = factureRepositoryPort;
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.factureDetailAssembler = factureDetailAssembler;
    }

    @Override
    @Transactional
    public FactureDetail soumettre(SoumettreFactureCommand command) {
        CommandeAchatId commandeAchatId = CommandeAchatId.of(command.commandeAchatId());
        FournisseurId fournisseurId = FournisseurId.of(command.fournisseurId());

        CommandeAchat commande = commandeAchatRepositoryPort.findById(commandeAchatId)
                .orElseThrow(CommandeAchatIntrouvableException::new);

        if (!commande.appartientA(fournisseurId)) {
            throw new AccesCommandeAchatRefuseException();
        }

        Facture facture = Facture.soumettre(commandeAchatId, fournisseurId, command.numeroFacture(),
                command.montant(), command.dateEmission(), command.dateEcheance(), command.pieceJointeMediaId());

        Facture saved = factureRepositoryPort.save(facture);
        return factureDetailAssembler.assembler(saved);
    }
}
