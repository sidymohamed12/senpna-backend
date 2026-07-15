package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.RejeterCommandeAchatCommand;

public interface RejeterCommandeAchatUseCase {
    CommandeAchatDetail rejeter(RejeterCommandeAchatCommand command);
}
