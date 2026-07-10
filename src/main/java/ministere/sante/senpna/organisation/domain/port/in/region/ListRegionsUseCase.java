package ministere.sante.senpna.organisation.domain.port.in.region;

import java.util.List;

import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;

public interface ListRegionsUseCase {
    List<RegionDetail> lister();
}
