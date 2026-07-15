package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ValiderCommandeAchatCommand;

public interface ValiderCommandeAchatUseCase {
    CommandeAchatDetail valider(ValiderCommandeAchatCommand command);
}
