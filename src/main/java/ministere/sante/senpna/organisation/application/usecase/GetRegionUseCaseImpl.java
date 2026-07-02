package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.RegionDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetRegionQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.in.GetRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetRegionUseCaseImpl implements GetRegionUseCase {

    private final RegionRepositoryPort regionRepositoryPort;
    private final RegionDetailAssembler regionDetailAssembler;

    public GetRegionUseCaseImpl(RegionRepositoryPort regionRepositoryPort,
            RegionDetailAssembler regionDetailAssembler) {
        this.regionRepositoryPort = regionRepositoryPort;
        this.regionDetailAssembler = regionDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public RegionDetail obtenir(GetRegionQuery query) {
        Region region = regionRepositoryPort.findById(RegionId.of(query.regionId()))
                .orElseThrow(RegionIntrouvableException::new);
        return regionDetailAssembler.assembler(region);
    }
}
