package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.exception.CodeStructureSanitaireDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.RegionInactiveException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.CreateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.shared.domain.exception.ValidationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enregistre une demande d'adhésion d'une structure sanitaire au réseau.
 * La structure est créée à l'état {@code EN_ATTENTE_VALIDATION} et
 * inactive.
 */
@Service
public class CreateStructureSanitaireUseCaseImpl implements CreateStructureSanitaireUseCase {

    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
    private final RegionRepositoryPort regionRepositoryPort;
    private final StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;

    public CreateStructureSanitaireUseCaseImpl(StructureSanitaireRepositoryPort structureSanitaireRepositoryPort,
            RegionRepositoryPort regionRepositoryPort,
            StructureSanitaireDetailAssembler structureSanitaireDetailAssembler) {
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
        this.regionRepositoryPort = regionRepositoryPort;
        this.structureSanitaireDetailAssembler = structureSanitaireDetailAssembler;
    }

    @Override
    @Transactional
    public StructureSanitaireDetail creer(CreateStructureSanitaireCommand command) {
        if (command.regionId() == null) {
            throw new ValidationException("La région de rattachement est obligatoire pour une demande d'adhésion",
                    "REGION_REQUIRED");
        }

        Region region = regionRepositoryPort.findById(RegionId.of(command.regionId()))
                .orElseThrow(RegionIntrouvableException::new);
        if (!region.isActif()) {
            throw new RegionInactiveException();
        }

        String code = command.code() != null ? command.code().trim().toUpperCase() : null;
        if (code != null && structureSanitaireRepositoryPort.existsByCode(code)) {
            throw new CodeStructureSanitaireDejaUtiliseException(code);
        }

        StructureSanitaire structure = StructureSanitaire.creer(command.code(), command.nom(), command.type(),
                region.getId(), command.district(), command.adresse(), command.telephone(), command.email(),
                command.responsableNom(), command.responsablePrenom());

        StructureSanitaire saved = structureSanitaireRepositoryPort.save(structure);
        return structureSanitaireDetailAssembler.assembler(saved);
    }
}
