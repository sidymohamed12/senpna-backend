package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.GetFactureQuery;

public interface GetFactureUseCase {
    FactureDetail obtenir(GetFactureQuery query);
}
