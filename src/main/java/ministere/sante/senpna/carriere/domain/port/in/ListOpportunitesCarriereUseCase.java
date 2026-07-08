package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.ListOpportunitesCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarrierePage;

public interface ListOpportunitesCarriereUseCase {
    OpportuniteCarrierePage lister(ListOpportunitesCarriereQuery query);
}
