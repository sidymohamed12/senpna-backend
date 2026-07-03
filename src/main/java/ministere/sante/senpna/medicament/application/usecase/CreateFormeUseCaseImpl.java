package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.FormeDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.exception.CodeFormeDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.port.in.CreateFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFormeUseCaseImpl implements CreateFormeUseCase {

    private final FormeRepositoryPort formeRepositoryPort;
    private final FormeDetailAssembler formeDetailAssembler;

    public CreateFormeUseCaseImpl(FormeRepositoryPort formeRepositoryPort,
            FormeDetailAssembler formeDetailAssembler) {
        this.formeRepositoryPort = formeRepositoryPort;
        this.formeDetailAssembler = formeDetailAssembler;
    }

    @Override
    @Transactional
    public FormeDetail creer(CreateFormeCommand command) {
        if (formeRepositoryPort.existsByCodeIgnoreCase(command.code().trim())) {
            throw new CodeFormeDejaUtiliseException(command.code());
        }

        Forme forme = Forme.creer(command.code(), command.libelle(), command.description());

        Forme saved = formeRepositoryPort.save(forme);
        return formeDetailAssembler.assembler(saved);
    }
}
