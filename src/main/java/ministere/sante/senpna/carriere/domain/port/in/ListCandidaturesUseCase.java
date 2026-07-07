package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidaturePage;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.ListCandidaturesQuery;

public interface ListCandidaturesUseCase {
    CandidaturePage lister(ListCandidaturesQuery query);
}
