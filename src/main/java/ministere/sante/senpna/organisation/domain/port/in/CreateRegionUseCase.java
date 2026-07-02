package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;

public interface CreateRegionUseCase {
    RegionDetail creer(CreateRegionCommand command);
}
