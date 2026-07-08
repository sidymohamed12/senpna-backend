package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.GetOpportuniteCarrierePubliqueUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetOpportuniteCarrierePubliqueUseCaseImpl implements GetOpportuniteCarrierePubliqueUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final OpportuniteCarriereDetailAssembler assembler;

    public GetOpportuniteCarrierePubliqueUseCaseImpl(
            OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public OpportuniteCarriereDetail obtenirPublique(GetOpportuniteCarriereQuery query) {
        OpportuniteCarriere opportunite = opportuniteCarriereRepositoryPort
                .findById(OpportuniteCarriereId.of(query.opportuniteId()))
                .filter(OpportuniteCarriere::estVisiblePubliquement)
                .orElseThrow(OpportuniteCarriereIntrouvableException::new);
        return assembler.assembler(opportunite);
    }
}
