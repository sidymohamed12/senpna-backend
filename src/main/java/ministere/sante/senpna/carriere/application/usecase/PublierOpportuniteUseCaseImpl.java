package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.PublierOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.PublierOpportuniteUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublierOpportuniteUseCaseImpl implements PublierOpportuniteUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final OpportuniteCarriereDetailAssembler assembler;

    public PublierOpportuniteUseCaseImpl(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public OpportuniteCarriereDetail publier(PublierOpportuniteCommand command) {
        OpportuniteCarriere opportunite = opportuniteCarriereRepositoryPort
                .findById(OpportuniteCarriereId.of(command.opportuniteId()))
                .orElseThrow(OpportuniteCarriereIntrouvableException::new);

        opportunite.publier();

        OpportuniteCarriere saved = opportuniteCarriereRepositoryPort.save(opportunite);
        return assembler.assembler(saved);
    }
}
