package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.EntrepotDetailAssembler;
import ministere.sante.senpna.organisation.application.service.RegionScopeResolver;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UpdatePraCommand;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.RegionInactiveException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.in.UpdatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdatePraUseCaseImpl implements UpdatePraUseCase {

    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final RegionRepositoryPort regionRepositoryPort;
    private final RegionScopeResolver regionScopeResolver;
    private final EntrepotDetailAssembler entrepotDetailAssembler;

    public UpdatePraUseCaseImpl(EntrepotRepositoryPort entrepotRepositoryPort,
            RegionRepositoryPort regionRepositoryPort, RegionScopeResolver regionScopeResolver,
            EntrepotDetailAssembler entrepotDetailAssembler) {
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.regionRepositoryPort = regionRepositoryPort;
        this.regionScopeResolver = regionScopeResolver;
        this.entrepotDetailAssembler = entrepotDetailAssembler;
    }

    @Override
    @Transactional
    public EntrepotDetail modifier(UpdatePraCommand command) {
        Entrepot pra = entrepotRepositoryPort.findById(EntrepotId.of(command.entrepotId()))
                .orElseThrow(EntrepotIntrouvableException::new);

        if (!pra.estPra()) {
            throw new TypeEntrepotInvalideException();
        }

        // Un utilisateur rattaché à une région ne peut gérer que les
        // entrepôts de cette région — évite qu'il modifie la PRA d'une
        // autre région. Les rôles nationaux (non rattachés) conservent un
        // accès à toutes les régions.
        regionScopeResolver.verifierAccesRegion(command.acteurId(), pra.getRegionId());

        RegionId regionId = pra.getRegionId();
        if (command.regionId() != null) {
            Region region = regionRepositoryPort.findById(RegionId.of(command.regionId()))
                    .orElseThrow(RegionIntrouvableException::new);
            if (!region.isActif()) {
                throw new RegionInactiveException();
            }
            regionId = region.getId();
        }

        pra.modifierInformations(command.nom(), command.adresse(), command.telephone(), regionId);

        Entrepot saved = entrepotRepositoryPort.save(pra);
        return entrepotDetailAssembler.assembler(saved);
    }
}
