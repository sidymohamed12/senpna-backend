package ministere.sante.senpna.commandeachat.application.service;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AvisExpeditionDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.LigneCommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.model.AvisExpedition;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;

import org.springframework.stereotype.Component;

@Component
public class CommandeAchatDetailAssembler {

    public CommandeAchatDetail assembler(CommandeAchat commande) {
        return new CommandeAchatDetail(
                commande.getId().getValue(),
                commande.getReference(),
                commande.getFournisseurId().getValue(),
                commande.getEntrepotDestinationId().getValue(),
                commande.getStatut(),
                commande.getLignes().stream().map(this::assemblerLigne).toList(),
                commande.getDateAccuseReceptionFournisseur(),
                commande.getDelaiLivraisonConfirmeJours(),
                commande.getDateLivraisonConfirmee(),
                assemblerAvis(commande.getAvisExpedition()),
                commande.getMotifRejet(),
                commande.getCommentaire(),
                commande.getCreatedAt(),
                commande.getUpdatedAt());
    }

    public CommandeAchatSummary assemblerResume(CommandeAchat commande) {
        return new CommandeAchatSummary(
                commande.getId().getValue(),
                commande.getReference(),
                commande.getFournisseurId().getValue(),
                commande.getStatut(),
                commande.getLignes().size(),
                commande.getCreatedAt());
    }

    private LigneCommandeAchatDetail assemblerLigne(LigneCommandeAchat ligne) {
        return new LigneCommandeAchatDetail(
                ligne.getId().getValue(),
                ligne.getMedicamentId().getValue(),
                ligne.getConditionnementId().getValue(),
                ligne.getQuantiteCommandee(),
                ligne.getPrixUnitaire(),
                ligne.getNumeroLot(),
                ligne.getDateFabrication(),
                ligne.getDateExpiration(),
                ligne.getCertificatAnalyseUrl(),
                ligne.getQuantiteExpediee(),
                ligne.getQuantiteRecue(),
                ligne.getQuantiteRefusee(),
                ligne.getMotifRefus());
    }

    private AvisExpeditionDetail assemblerAvis(AvisExpedition avis) {
        if (avis == null) {
            return null;
        }
        return new AvisExpeditionDetail(avis.getDateExpedition(), avis.getTransporteur(), avis.getNumeroSuivi(),
                avis.getDateLivraisonEstimee());
    }
}
