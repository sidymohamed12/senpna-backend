package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ValiderFactureCommand;

public interface ValiderFactureUseCase {
    FactureDetail valider(ValiderFactureCommand command);
}
