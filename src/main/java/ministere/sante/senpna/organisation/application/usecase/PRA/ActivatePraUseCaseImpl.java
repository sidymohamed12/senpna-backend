package ministere.sante.senpna.organisation.application.usecase.pra;

import ministere.sante.senpna.organisation.application.service.EntrepotDetailAssembler;
import ministere.sante.senpna.organisation.application.service.RegionScopeResolver;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ActivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.in.pra.ActivatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivatePraUseCaseImpl implements ActivatePraUseCase {

    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final RegionScopeResolver regionScopeResolver;
    private final EntrepotDetailAssembler entrepotDetailAssembler;

    public ActivatePraUseCaseImpl(EntrepotRepositoryPort entrepotRepositoryPort,
            RegionScopeResolver regionScopeResolver, EntrepotDetailAssembler entrepotDetailAssembler) {
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.regionScopeResolver = regionScopeResolver;
        this.entrepotDetailAssembler = entrepotDetailAssembler;
    }

    @Override
    @Transactional
    public EntrepotDetail activer(ActivatePraCommand command) {
        Entrepot pra = entrepotRepositoryPort.findById(EntrepotId.of(command.entrepotId()))
                .orElseThrow(EntrepotIntrouvableException::new);

        if (!pra.estPra()) {
            throw new TypeEntrepotInvalideException();
        }

        regionScopeResolver.verifierAccesRegion(command.acteurId(), pra.getRegionId());

        pra.activer();

        Entrepot saved = entrepotRepositoryPort.save(pra);
        return entrepotDetailAssembler.assembler(saved);
    }
}
