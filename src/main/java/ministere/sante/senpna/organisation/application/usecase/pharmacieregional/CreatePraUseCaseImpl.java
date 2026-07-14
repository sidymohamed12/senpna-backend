package ministere.sante.senpna.organisation.application.usecase.pharmacieregional;

import ministere.sante.senpna.organisation.application.service.EntrepotDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.Entrepot.CreatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.exception.CodeEntrepotDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.RegionInactiveException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.port.in.pra.CreatePraUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePraUseCaseImpl implements CreatePraUseCase {

    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final RegionRepositoryPort regionRepositoryPort;
    private final EntrepotDetailAssembler entrepotDetailAssembler;

    public CreatePraUseCaseImpl(EntrepotRepositoryPort entrepotRepositoryPort,
            RegionRepositoryPort regionRepositoryPort, EntrepotDetailAssembler entrepotDetailAssembler) {
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.regionRepositoryPort = regionRepositoryPort;
        this.entrepotDetailAssembler = entrepotDetailAssembler;
    }

    @Override
    @Transactional
    public EntrepotDetail creer(CreatePraCommand command) {
        if (command.regionId() == null) {
            throw new SenPnaException("La région de rattachement est obligatoire pour une PRA",
                    "REGION_REQUIRED", ErrorCategory.VALIDATION);
        }

        Region region = regionRepositoryPort.findById(RegionId.of(command.regionId()))
                .orElseThrow(RegionIntrouvableException::new);
        if (!region.isActif()) {
            throw new RegionInactiveException();
        }

        String code = command.code() != null ? command.code().trim().toUpperCase() : null;
        if (code != null && entrepotRepositoryPort.existsByCode(code)) {
            throw new CodeEntrepotDejaUtiliseException(code);
        }

        Entrepot pra = Entrepot.creerPra(command.code(), command.nom(), region.getId(), command.adresse(),
                command.telephone());

        Entrepot saved = entrepotRepositoryPort.save(pra);
        return entrepotDetailAssembler.assembler(saved);
    }
}
