package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;

public interface GetCommandeAchatUseCase {
    CommandeAchatDetail obtenir(GetCommandeAchatQuery query);
}
