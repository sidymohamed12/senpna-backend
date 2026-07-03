package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.exception.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.port.in.DesarchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesarchiveConditionnementUseCaseImpl implements DesarchiveConditionnementUseCase {

    private final ConditionnementRepositoryPort conditionnementRepositoryPort;
    private final ConditionnementDetailAssembler conditionnementDetailAssembler;

    public DesarchiveConditionnementUseCaseImpl(ConditionnementRepositoryPort conditionnementRepositoryPort,
            ConditionnementDetailAssembler conditionnementDetailAssembler) {
        this.conditionnementRepositoryPort = conditionnementRepositoryPort;
        this.conditionnementDetailAssembler = conditionnementDetailAssembler;
    }

    @Override
    @Transactional
    public ConditionnementDetail desarchiver(DesarchiveConditionnementCommand command) {
        Conditionnement conditionnement = conditionnementRepositoryPort
                .findById(ConditionnementId.of(command.conditionnementId()))
                .orElseThrow(ConditionnementIntrouvableException::new);

        conditionnement.desarchiver();

        Conditionnement saved = conditionnementRepositoryPort.save(conditionnement);
        return conditionnementDetailAssembler.assembler(saved);
    }
}
