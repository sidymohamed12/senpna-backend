package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RejectAdhesionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.RejectAdhesionUseCase;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectAdhesionUseCaseImpl implements RejectAdhesionUseCase {

    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
    private final StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;

    public RejectAdhesionUseCaseImpl(StructureSanitaireRepositoryPort structureSanitaireRepositoryPort,
            StructureSanitaireDetailAssembler structureSanitaireDetailAssembler) {
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
        this.structureSanitaireDetailAssembler = structureSanitaireDetailAssembler;
    }

    @Override
    @Transactional
    public StructureSanitaireDetail rejeter(RejectAdhesionCommand command) {
        StructureSanitaire structure = structureSanitaireRepositoryPort
                .findById(StructureSanitaireId.of(command.structureId()))
                .orElseThrow(StructureSanitaireIntrouvableException::new);

        structure.rejeterAdhesion(command.motif());

        StructureSanitaire saved = structureSanitaireRepositoryPort.save(structure);
        return structureSanitaireDetailAssembler.assembler(saved);
    }
}
