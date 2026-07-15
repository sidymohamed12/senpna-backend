package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.MarquerFacturePayeeCommand;

public interface MarquerFacturePayeeUseCase {
    FactureDetail marquerPayee(MarquerFacturePayeeCommand command);
}
