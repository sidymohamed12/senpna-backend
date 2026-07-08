package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.GetOpportuniteCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetOpportuniteCarriereUseCaseImpl implements GetOpportuniteCarriereUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final OpportuniteCarriereDetailAssembler assembler;

    public GetOpportuniteCarriereUseCaseImpl(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public OpportuniteCarriereDetail obtenir(GetOpportuniteCarriereQuery query) {
        OpportuniteCarriere opportunite = opportuniteCarriereRepositoryPort
                .findById(OpportuniteCarriereId.of(query.opportuniteId()))
                .orElseThrow(OpportuniteCarriereIntrouvableException::new);
        return assembler.assembler(opportunite);
    }
}
