package ministere.sante.senpna.medicament.application.usecase.forme;

import ministere.sante.senpna.medicament.application.service.FormeDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.ArchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.port.in.forme.ArchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.shared.domain.port.out.FormeCachePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveFormeUseCaseImpl implements ArchiveFormeUseCase {

    private final FormeRepositoryPort formeRepositoryPort;
    private final FormeCachePort formeCachePort;
    private final FormeDetailAssembler formeDetailAssembler;

    public ArchiveFormeUseCaseImpl(FormeRepositoryPort formeRepositoryPort, FormeCachePort formeCachePort,
            FormeDetailAssembler formeDetailAssembler) {
        this.formeRepositoryPort = formeRepositoryPort;
        this.formeCachePort = formeCachePort;
        this.formeDetailAssembler = formeDetailAssembler;
    }

    @Override
    @Transactional
    public FormeDetail archiver(ArchiveFormeCommand command) {
        Forme forme = formeRepositoryPort.findById(FormeId.of(command.formeId()))
                .orElseThrow(FormeIntrouvableException::new);

        forme.archiver();

        Forme saved = formeRepositoryPort.save(forme);
        formeCachePort.reload();

        return formeDetailAssembler.assembler(saved);
    }
}
