package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetEntrepotQuery;

public interface GetEntrepotUseCase {
    EntrepotDetail obtenir(GetEntrepotQuery query);
}
