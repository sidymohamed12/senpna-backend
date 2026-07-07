package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;

public interface GetOpportuniteCarriereUseCase {
    OpportuniteCarriereDetail obtenir(GetOpportuniteCarriereQuery query);
}
