package ministere.sante.senpna.carriere.domain.port.in;

import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.GetCandidatureQuery;

public interface GetCandidatureUseCase {
    CandidatureDetail obtenir(GetCandidatureQuery query);
}
