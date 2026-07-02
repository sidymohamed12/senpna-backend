package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.RegionDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RegionDetail;
import ministere.sante.senpna.organisation.domain.exception.CodeRegionDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.in.CreateRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRegionUseCaseImpl implements CreateRegionUseCase {

    private final RegionRepositoryPort regionRepositoryPort;
    private final RegionDetailAssembler regionDetailAssembler;

    public CreateRegionUseCaseImpl(RegionRepositoryPort regionRepositoryPort,
            RegionDetailAssembler regionDetailAssembler) {
        this.regionRepositoryPort = regionRepositoryPort;
        this.regionDetailAssembler = regionDetailAssembler;
    }

    @Override
    @Transactional
    public RegionDetail creer(CreateRegionCommand command) {
        String code = command.code() != null ? command.code().trim().toUpperCase() : null;
        if (code != null && regionRepositoryPort.existsByCode(code)) {
            throw new CodeRegionDejaUtiliseException(code);
        }

        Region region = Region.creer(command.code(), command.nom());
        Region saved = regionRepositoryPort.save(region);
        return regionDetailAssembler.assembler(saved);
    }
}
