package ministere.sante.senpna.organisation.application.usecase.strcuture;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ActivateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.structure.ActivateStructureSanitaireUseCase;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateStructureSanitaireUseCaseImpl implements ActivateStructureSanitaireUseCase {

    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
    private final StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;

    public ActivateStructureSanitaireUseCaseImpl(StructureSanitaireRepositoryPort structureSanitaireRepositoryPort,
            StructureSanitaireDetailAssembler structureSanitaireDetailAssembler) {
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
        this.structureSanitaireDetailAssembler = structureSanitaireDetailAssembler;
    }

    @Override
    @Transactional
    public StructureSanitaireDetail activer(ActivateStructureSanitaireCommand command) {
        StructureSanitaire structure = structureSanitaireRepositoryPort
                .findById(StructureSanitaireId.of(command.structureId()))
                .orElseThrow(StructureSanitaireIntrouvableException::new);

        structure.activer();

        StructureSanitaire saved = structureSanitaireRepositoryPort.save(structure);
        return structureSanitaireDetailAssembler.assembler(saved);
    }
}
