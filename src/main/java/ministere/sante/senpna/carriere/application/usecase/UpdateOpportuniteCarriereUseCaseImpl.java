package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereCommandMapper;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.UpdateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.UpdateOpportuniteCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOpportuniteCarriereUseCaseImpl implements UpdateOpportuniteCarriereUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final OpportuniteCarriereCommandMapper commandMapper;
    private final OpportuniteCarriereDetailAssembler assembler;

    public UpdateOpportuniteCarriereUseCaseImpl(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            OpportuniteCarriereCommandMapper commandMapper, OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public OpportuniteCarriereDetail modifier(UpdateOpportuniteCarriereCommand command) {
        OpportuniteCarriere opportunite = opportuniteCarriereRepositoryPort
                .findById(OpportuniteCarriereId.of(command.opportuniteId()))
                .orElseThrow(OpportuniteCarriereIntrouvableException::new);

        TypeContrat typeContrat = commandMapper.versTypeContrat(command.typeContrat());

        opportunite.modifierContenu(command.titre(), command.nomEntreprise(), command.description(),
                command.ficheDePosteUrl(), command.lieu(), typeContrat, command.dateDebut(),
                command.dateLimiteCandidature(), command.emailContact());

        OpportuniteCarriere saved = opportuniteCarriereRepositoryPort.save(opportunite);

        return assembler.assembler(saved);
    }
}
