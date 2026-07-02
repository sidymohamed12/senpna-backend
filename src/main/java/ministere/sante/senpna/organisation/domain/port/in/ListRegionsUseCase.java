package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;

import java.util.List;

public interface ListRegionsUseCase {
    List<RegionDetail> lister();
}
