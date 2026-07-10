package ministere.sante.senpna.medicament.application.usecase.conditionnement;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.DerniereUniteBaseException;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.ArchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveConditionnementUseCaseImpl implements ArchiveConditionnementUseCase {

    private final ConditionnementRepositoryPort conditionnementRepositoryPort;
    private final ConditionnementDetailAssembler conditionnementDetailAssembler;

    public ArchiveConditionnementUseCaseImpl(ConditionnementRepositoryPort conditionnementRepositoryPort,
            ConditionnementDetailAssembler conditionnementDetailAssembler) {
        this.conditionnementRepositoryPort = conditionnementRepositoryPort;
        this.conditionnementDetailAssembler = conditionnementDetailAssembler;
    }

    @Override
    @Transactional
    public ConditionnementDetail archiver(ArchiveConditionnementCommand command) {
        Conditionnement conditionnement = conditionnementRepositoryPort
                .findById(ConditionnementId.of(command.conditionnementId()))
                .orElseThrow(ConditionnementIntrouvableException::new);

        if (conditionnement.isEstUniteBase() && conditionnementRepositoryPort
                .estUniqueUniteBaseActive(conditionnement.getMedicamentId(), conditionnement.getId())) {
            throw new DerniereUniteBaseException();
        }

        conditionnement.archiver();

        Conditionnement saved = conditionnementRepositoryPort.save(conditionnement);
        return conditionnementDetailAssembler.assembler(saved);
    }
}
