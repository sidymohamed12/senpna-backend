package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.PublierOpportuniteCommand;

public interface PublierOpportuniteUseCase {
    OpportuniteCarriereDetail publier(PublierOpportuniteCommand command);
}
