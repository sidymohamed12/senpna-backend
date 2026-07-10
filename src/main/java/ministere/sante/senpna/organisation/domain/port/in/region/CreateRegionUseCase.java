package ministere.sante.senpna.organisation.domain.port.in.region;

import ministere.sante.senpna.organisation.domain.command.RegionCommand.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;

public interface CreateRegionUseCase {
    RegionDetail creer(CreateRegionCommand command);
}
