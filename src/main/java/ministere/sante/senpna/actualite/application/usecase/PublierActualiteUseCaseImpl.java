package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.PublierActualiteCommand;
import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.in.PublierActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublierActualiteUseCaseImpl implements PublierActualiteUseCase {

    private final ActualiteRepositoryPort actualiteRepositoryPort;
    private final ActualiteDetailAssembler assembler;

    public PublierActualiteUseCaseImpl(ActualiteRepositoryPort actualiteRepositoryPort,
            ActualiteDetailAssembler assembler) {
        this.actualiteRepositoryPort = actualiteRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ActualiteDetail publier(PublierActualiteCommand command) {
        Actualite actualite = actualiteRepositoryPort.findById(ActualiteId.of(command.actualiteId()))
                .orElseThrow(ActualiteIntrouvableException::new);

        actualite.publier();

        Actualite saved = actualiteRepositoryPort.save(actualite);
        return assembler.assembler(saved);
    }
}
