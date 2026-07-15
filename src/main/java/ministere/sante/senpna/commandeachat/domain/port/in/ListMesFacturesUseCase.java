package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListMesFacturesQuery;

public interface ListMesFacturesUseCase {
    FacturePage lister(ListMesFacturesQuery query);
}
