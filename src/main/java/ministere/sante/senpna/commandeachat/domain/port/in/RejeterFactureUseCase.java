package ministere.sante.senpna.commandeachat.domain.port.in;

import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.RejeterFactureCommand;

public interface RejeterFactureUseCase {
    FactureDetail rejeter(RejeterFactureCommand command);
}
