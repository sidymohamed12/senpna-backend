package ministere.sante.senpna.organisation.domain.port.in.region;

import ministere.sante.senpna.organisation.domain.command.RegionCommand.GetRegionQuery;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;

public interface GetRegionUseCase {
    RegionDetail obtenir(GetRegionQuery query);
}
