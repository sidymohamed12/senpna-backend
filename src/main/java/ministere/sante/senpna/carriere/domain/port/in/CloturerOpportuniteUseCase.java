package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CloturerOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;

public interface CloturerOpportuniteUseCase {
    OpportuniteCarriereDetail cloturer(CloturerOpportuniteCommand command);
}
