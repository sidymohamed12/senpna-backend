package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ReceptionnerCommandeAchatCommand;

public interface ReceptionnerCommandeAchatUseCase {
    CommandeAchatDetail receptionner(ReceptionnerCommandeAchatCommand command);
}
