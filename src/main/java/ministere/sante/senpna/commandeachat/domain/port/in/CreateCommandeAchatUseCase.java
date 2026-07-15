package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CreateCommandeAchatCommand;

public interface CreateCommandeAchatUseCase {
    CommandeAchatDetail creer(CreateCommandeAchatCommand command);
}
