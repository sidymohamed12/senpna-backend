package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GenererAvisExpeditionCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.InfoExpeditionLigneInput;
import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.AvisExpedition;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat.InfoExpeditionLigne;
import ministere.sante.senpna.commandeachat.domain.port.in.GenererAvisExpeditionUseCase;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.LigneCommandeAchatId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Génération de l'avis d'expédition par le fournisseur (cf. doc. métier
 * « Suivi des Livraisons » : suivi des lots, dates de péremption,
 * certificats d'analyse). Fait passer la commande {@code EN_TRANSIT} à
 * {@code EXPEDIEE} — au-delà, plus aucune modification n'est permise (cf.
 * règles métier « une commande expédiée ne peut plus être modifiée »).
 */
@Service
public class GenererAvisExpeditionUseCaseImpl implements GenererAvisExpeditionUseCase {

    private final CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    private final CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    public GenererAvisExpeditionUseCaseImpl(CommandeAchatRepositoryPort commandeAchatRepositoryPort,
            CommandeAchatDetailAssembler commandeAchatDetailAssembler) {
        this.commandeAchatRepositoryPort = commandeAchatRepositoryPort;
        this.commandeAchatDetailAssembler = commandeAchatDetailAssembler;
    }

    @Override
    @Transactional
    public CommandeAchatDetail generer(GenererAvisExpeditionCommand command) {
        CommandeAchat commande = commandeAchatRepositoryPort.findById(CommandeAchatId.of(command.commandeAchatId()))
                .orElseThrow(CommandeAchatIntrouvableException::new);

        if (!commande.appartientA(FournisseurId.of(command.fournisseurId()))) {
            throw new AccesCommandeAchatRefuseException();
        }

        AvisExpedition avis = AvisExpedition.of(command.dateExpedition(), command.transporteur(),
                command.numeroSuivi(), command.dateLivraisonEstimee());

        commande.genererAvisExpedition(avis, command.lignes().stream().map(this::toInfo).toList());

        CommandeAchat saved = commandeAchatRepositoryPort.save(commande);
        return commandeAchatDetailAssembler.assembler(saved);
    }

    private InfoExpeditionLigne toInfo(InfoExpeditionLigneInput input) {
        return new InfoExpeditionLigne(LigneCommandeAchatId.of(input.ligneId()), input.numeroLot(),
                input.dateFabrication(), input.dateExpiration(), input.certificatAnalyseUrl(),
                input.quantiteExpediee());
    }
}
