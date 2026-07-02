package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetRegionQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;

public interface GetRegionUseCase {
    RegionDetail obtenir(GetRegionQuery query);
}
