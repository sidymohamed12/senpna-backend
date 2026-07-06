package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.DesactiverActualiteCommand;
import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.in.DesactiverActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesactiverActualiteUseCaseImpl implements DesactiverActualiteUseCase {

    private final ActualiteRepositoryPort actualiteRepositoryPort;
    private final ActualiteDetailAssembler assembler;

    public DesactiverActualiteUseCaseImpl(ActualiteRepositoryPort actualiteRepositoryPort,
            ActualiteDetailAssembler assembler) {
        this.actualiteRepositoryPort = actualiteRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ActualiteDetail desactiver(DesactiverActualiteCommand command) {
        Actualite actualite = actualiteRepositoryPort.findById(ActualiteId.of(command.actualiteId()))
                .orElseThrow(ActualiteIntrouvableException::new);

        actualite.desactiver();

        Actualite saved = actualiteRepositoryPort.save(actualite);
        return assembler.assembler(saved);
    }
}
