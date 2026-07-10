package ministere.sante.senpna.organisation.application.usecase.region;

import ministere.sante.senpna.organisation.application.service.RegionDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.CreateRegionCommand;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;
import ministere.sante.senpna.organisation.domain.exception.CodeRegionDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.in.region.CreateRegionUseCase;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.RegionCachePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRegionUseCaseImpl implements CreateRegionUseCase {

    private final RegionRepositoryPort regionRepositoryPort;
    private final RegionCachePort regionCachePort;
    private final RegionDetailAssembler regionDetailAssembler;

    public CreateRegionUseCaseImpl(RegionRepositoryPort regionRepositoryPort, RegionCachePort regionCachePort,
            RegionDetailAssembler regionDetailAssembler) {
        this.regionRepositoryPort = regionRepositoryPort;
        this.regionCachePort = regionCachePort;
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

        // Recharge le cache immédiatement : la nouvelle région doit être
        // utilisable sans délai par les autres features (ex: création
        // d'une PRA dans la foulée), sans attendre un redémarrage.
        regionCachePort.reload();

        return regionDetailAssembler.assembler(saved);
    }
}
