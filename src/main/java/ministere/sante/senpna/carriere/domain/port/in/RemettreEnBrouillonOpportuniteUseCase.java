package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.RemettreEnBrouillonOpportuniteCommand;

public interface RemettreEnBrouillonOpportuniteUseCase {
    OpportuniteCarriereDetail remettreEnBrouillon(RemettreEnBrouillonOpportuniteCommand command);
}
