package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CreateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;

public interface CreateOpportuniteCarriereUseCase {
    OpportuniteCarriereDetail creer(CreateOpportuniteCarriereCommand command);
}
