package ministere.sante.senpna.commandeachat.application.facade;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AccuserReceptionCommandeCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ConfirmerDelaiLivraisonCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GenererAvisExpeditionCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListMesCommandesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.GetFactureQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListMesFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.SoumettreFactureCommand;
import ministere.sante.senpna.commandeachat.domain.port.in.AccuserReceptionCommandeUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ConfirmerDelaiLivraisonUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GenererAvisExpeditionUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GetCommandeAchatUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.GetFactureUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListMesCommandesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.ListMesFacturesUseCase;
import ministere.sante.senpna.commandeachat.domain.port.in.SoumettreFactureUseCase;

import org.springframework.stereotype.Component;

/**
 * Façade côté espace fournisseur — réception et traitement des bons de
 * commande émis par la PNA, mise à jour des informations de livraison, et
 * soumission des factures.
 */
@Component
public class EspaceFournisseurCommandeAchatFacade {

    private final AccuserReceptionCommandeUseCase accuserReceptionCommandeUseCase;
    private final ConfirmerDelaiLivraisonUseCase confirmerDelaiLivraisonUseCase;
    private final GenererAvisExpeditionUseCase genererAvisExpeditionUseCase;
    private final GetCommandeAchatUseCase getCommandeAchatUseCase;
    private final ListMesCommandesUseCase listMesCommandesUseCase;
    private final SoumettreFactureUseCase soumettreFactureUseCase;
    private final GetFactureUseCase getFactureUseCase;
    private final ListMesFacturesUseCase listMesFacturesUseCase;

    public EspaceFournisseurCommandeAchatFacade(
            AccuserReceptionCommandeUseCase accuserReceptionCommandeUseCase,
            ConfirmerDelaiLivraisonUseCase confirmerDelaiLivraisonUseCase,
            GenererAvisExpeditionUseCase genererAvisExpeditionUseCase,
            GetCommandeAchatUseCase getCommandeAchatUseCase,
            ListMesCommandesUseCase listMesCommandesUseCase,
            SoumettreFactureUseCase soumettreFactureUseCase,
            GetFactureUseCase getFactureUseCase,
            ListMesFacturesUseCase listMesFacturesUseCase) {
        this.accuserReceptionCommandeUseCase = accuserReceptionCommandeUseCase;
        this.confirmerDelaiLivraisonUseCase = confirmerDelaiLivraisonUseCase;
        this.genererAvisExpeditionUseCase = genererAvisExpeditionUseCase;
        this.getCommandeAchatUseCase = getCommandeAchatUseCase;
        this.listMesCommandesUseCase = listMesCommandesUseCase;
        this.soumettreFactureUseCase = soumettreFactureUseCase;
        this.getFactureUseCase = getFactureUseCase;
        this.listMesFacturesUseCase = listMesFacturesUseCase;
    }

    public CommandeAchatDetail accuserReception(AccuserReceptionCommandeCommand command) {
        return accuserReceptionCommandeUseCase.accuserReception(command);
    }

    public CommandeAchatDetail confirmerDelaiLivraison(ConfirmerDelaiLivraisonCommand command) {
        return confirmerDelaiLivraisonUseCase.confirmer(command);
    }

    public CommandeAchatDetail genererAvisExpedition(GenererAvisExpeditionCommand command) {
        return genererAvisExpeditionUseCase.generer(command);
    }

    public CommandeAchatDetail obtenirCommande(GetCommandeAchatQuery query) {
        return getCommandeAchatUseCase.obtenir(query);
    }

    public CommandeAchatPage listerMesCommandes(ListMesCommandesQuery query) {
        return listMesCommandesUseCase.lister(query);
    }

    public FactureDetail soumettreFacture(SoumettreFactureCommand command) {
        return soumettreFactureUseCase.soumettre(command);
    }

    public FactureDetail obtenirFacture(GetFactureQuery query) {
        return getFactureUseCase.obtenir(query);
    }

    public FacturePage listerMesFactures(ListMesFacturesQuery query) {
        return listMesFacturesUseCase.lister(query);
    }
}
