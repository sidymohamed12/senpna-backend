package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.RemettreEnBrouillonActualiteCommand;
import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.in.RemettreEnBrouillonActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemettreEnBrouillonActualiteUseCaseImpl implements RemettreEnBrouillonActualiteUseCase {

    private final ActualiteRepositoryPort actualiteRepositoryPort;
    private final ActualiteDetailAssembler assembler;

    public RemettreEnBrouillonActualiteUseCaseImpl(ActualiteRepositoryPort actualiteRepositoryPort,
            ActualiteDetailAssembler assembler) {
        this.actualiteRepositoryPort = actualiteRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ActualiteDetail remettreEnBrouillon(RemettreEnBrouillonActualiteCommand command) {
        Actualite actualite = actualiteRepositoryPort.findById(ActualiteId.of(command.actualiteId()))
                .orElseThrow(ActualiteIntrouvableException::new);

        actualite.remettreEnBrouillon();

        Actualite saved = actualiteRepositoryPort.save(actualite);
        return assembler.assembler(saved);
    }
}
