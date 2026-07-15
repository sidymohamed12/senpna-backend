package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AccuserReceptionCommandeCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;

/** Fournisseur — accusé de réception du bon de commande. */
public interface AccuserReceptionCommandeUseCase {
    CommandeAchatDetail accuserReception(AccuserReceptionCommandeCommand command);
}
