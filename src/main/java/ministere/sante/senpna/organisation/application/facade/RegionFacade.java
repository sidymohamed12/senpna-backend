package ministere.sante.senpna.organisation.application.facade;

import java.util.List;

import ministere.sante.senpna.organisation.domain.command.RegionCommand.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.GetRegionQuery;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;
import ministere.sante.senpna.organisation.domain.port.in.region.CreateRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.region.GetRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.in.region.ListRegionsUseCase;

public class RegionFacade {

    private final CreateRegionUseCase createRegionUseCase;
    private final GetRegionUseCase getRegionUseCase;
    private final ListRegionsUseCase listRegionsUseCase;

    public RegionFacade(CreateRegionUseCase createRegionUseCase, GetRegionUseCase getRegionUseCase,
            ListRegionsUseCase listRegionsUseCase) {
        this.createRegionUseCase = createRegionUseCase;
        this.getRegionUseCase = getRegionUseCase;
        this.listRegionsUseCase = listRegionsUseCase;
    }

    public RegionDetail creerRegion(CreateRegionCommand command) {
        return createRegionUseCase.creer(command);
    }

    public RegionDetail obtenirRegion(GetRegionQuery query) {
        return getRegionUseCase.obtenir(query);
    }

    public List<RegionDetail> listerRegions() {
        return listRegionsUseCase.lister();
    }
}
