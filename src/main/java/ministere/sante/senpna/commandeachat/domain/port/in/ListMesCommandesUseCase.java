package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListMesCommandesQuery;

public interface ListMesCommandesUseCase {
    CommandeAchatPage lister(ListMesCommandesQuery query);
}
