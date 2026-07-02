package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.RegionDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;
import ministere.sante.senpna.organisation.domain.port.in.ListRegionsUseCase;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListRegionsUseCaseImpl implements ListRegionsUseCase {

    private final RegionRepositoryPort regionRepositoryPort;
    private final RegionDetailAssembler regionDetailAssembler;

    public ListRegionsUseCaseImpl(RegionRepositoryPort regionRepositoryPort,
            RegionDetailAssembler regionDetailAssembler) {
        this.regionRepositoryPort = regionRepositoryPort;
        this.regionDetailAssembler = regionDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionDetail> lister() {
        return regionRepositoryPort.findAll().stream().map(regionDetailAssembler::assembler).toList();
    }
}
