package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ConfirmerDelaiLivraisonCommand;

/** Fournisseur — confirmation du délai de livraison. */
public interface ConfirmerDelaiLivraisonUseCase {
    CommandeAchatDetail confirmer(ConfirmerDelaiLivraisonCommand command);
}
