package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.UpdateOpportuniteCarriereCommand;

public interface UpdateOpportuniteCarriereUseCase {
    OpportuniteCarriereDetail modifier(UpdateOpportuniteCarriereCommand command);
}
