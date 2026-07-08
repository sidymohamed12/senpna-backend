package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CloturerOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.CloturerOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloturerOpportuniteUseCaseImpl implements CloturerOpportuniteUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final OpportuniteCarriereDetailAssembler assembler;

    public CloturerOpportuniteUseCaseImpl(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public OpportuniteCarriereDetail cloturer(CloturerOpportuniteCommand command) {
        OpportuniteCarriere opportunite = opportuniteCarriereRepositoryPort
                .findById(OpportuniteCarriereId.of(command.opportuniteId()))
                .orElseThrow(OpportuniteCarriereIntrouvableException::new);

        opportunite.cloturer();

        OpportuniteCarriere saved = opportuniteCarriereRepositoryPort.save(opportunite);
        return assembler.assembler(saved);
    }
}
