package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetConditionnementQuery;
import ministere.sante.senpna.medicament.domain.exception.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.port.in.GetConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetConditionnementUseCaseImpl implements GetConditionnementUseCase {

    private final ConditionnementRepositoryPort conditionnementRepositoryPort;
    private final ConditionnementDetailAssembler conditionnementDetailAssembler;

    public GetConditionnementUseCaseImpl(ConditionnementRepositoryPort conditionnementRepositoryPort,
            ConditionnementDetailAssembler conditionnementDetailAssembler) {
        this.conditionnementRepositoryPort = conditionnementRepositoryPort;
        this.conditionnementDetailAssembler = conditionnementDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ConditionnementDetail obtenir(GetConditionnementQuery query) {
        Conditionnement conditionnement = conditionnementRepositoryPort
                .findById(ConditionnementId.of(query.conditionnementId()))
                .orElseThrow(ConditionnementIntrouvableException::new);
        return conditionnementDetailAssembler.assembler(conditionnement);
    }
}
