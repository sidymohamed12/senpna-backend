package ministere.sante.senpna.organisation.application.usecase.affectation;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignStructureToPraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.affectation.AssignStructureToPraUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignStructureToPraUseCaseImpl implements AssignStructureToPraUseCase {

    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;

    public AssignStructureToPraUseCaseImpl(StructureSanitaireRepositoryPort structureSanitaireRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort,
            StructureSanitaireDetailAssembler structureSanitaireDetailAssembler) {
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.structureSanitaireDetailAssembler = structureSanitaireDetailAssembler;
    }

    @Override
    @Transactional
    public StructureSanitaireDetail affecter(AssignStructureToPraCommand command) {
        StructureSanitaire structure = structureSanitaireRepositoryPort
                .findById(StructureSanitaireId.of(command.structureId()))
                .orElseThrow(StructureSanitaireIntrouvableException::new);

        Entrepot pra = entrepotRepositoryPort.findById(EntrepotId.of(command.praId()))
                .orElseThrow(EntrepotIntrouvableException::new);
        if (!pra.estPra()) {
            throw new TypeEntrepotInvalideException();
        }
        if (!pra.isActif()) {
            throw new EntrepotInactifException();
        }

        structure.affecterPra(pra.getId());

        StructureSanitaire saved = structureSanitaireRepositoryPort.save(structure);
        return structureSanitaireDetailAssembler.assembler(saved);
    }
}
