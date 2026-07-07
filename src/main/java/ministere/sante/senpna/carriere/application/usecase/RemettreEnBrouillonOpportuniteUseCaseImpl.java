package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.RemettreEnBrouillonOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.RemettreEnBrouillonOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemettreEnBrouillonOpportuniteUseCaseImpl implements RemettreEnBrouillonOpportuniteUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final OpportuniteCarriereDetailAssembler assembler;

    public RemettreEnBrouillonOpportuniteUseCaseImpl(
            OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public OpportuniteCarriereDetail remettreEnBrouillon(RemettreEnBrouillonOpportuniteCommand command) {
        OpportuniteCarriere opportunite = opportuniteCarriereRepositoryPort
                .findById(OpportuniteCarriereId.of(command.opportuniteId()))
                .orElseThrow(OpportuniteCarriereIntrouvableException::new);

        opportunite.remettreEnBrouillon();

        OpportuniteCarriere saved = opportuniteCarriereRepositoryPort.save(opportunite);
        return assembler.assembler(saved);
    }
}
