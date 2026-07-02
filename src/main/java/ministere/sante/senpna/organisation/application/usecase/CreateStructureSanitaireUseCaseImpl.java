package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.exception.CodeStructureSanitaireDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.CreateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enregistre une demande d'adhésion d'une structure sanitaire au réseau.
 * La structure est créée à l'état {@code EN_ATTENTE_VALIDATION} et
 * inactive — elle ne pourra recevoir de commandes qu'après validation par
 * la PNA/PRA (cf. {@link ValidateAdhesionUseCaseImpl}).
 */
@Service
public class CreateStructureSanitaireUseCaseImpl implements CreateStructureSanitaireUseCase {

    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
    private final StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;

    public CreateStructureSanitaireUseCaseImpl(StructureSanitaireRepositoryPort structureSanitaireRepositoryPort,
            StructureSanitaireDetailAssembler structureSanitaireDetailAssembler) {
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
        this.structureSanitaireDetailAssembler = structureSanitaireDetailAssembler;
    }

    @Override
    @Transactional
    public StructureSanitaireDetail creer(CreateStructureSanitaireCommand command) {
        String code = command.code() != null ? command.code().trim().toUpperCase() : null;
        if (code != null && structureSanitaireRepositoryPort.existsByCode(code)) {
            throw new CodeStructureSanitaireDejaUtiliseException(code);
        }

        StructureSanitaire structure = StructureSanitaire.creer(command.code(), command.nom(), command.type(),
                command.district(), command.adresse(), command.telephone(), command.email(),
                command.responsable());

        StructureSanitaire saved = structureSanitaireRepositoryPort.save(structure);
        return structureSanitaireDetailAssembler.assembler(saved);
    }
}
