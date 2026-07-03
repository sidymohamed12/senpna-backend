package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.FormeDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.exception.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.port.in.DesarchiveFormeUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesarchiveFormeUseCaseImpl implements DesarchiveFormeUseCase {

    private final FormeRepositoryPort formeRepositoryPort;
    private final FormeDetailAssembler formeDetailAssembler;

    public DesarchiveFormeUseCaseImpl(FormeRepositoryPort formeRepositoryPort,
            FormeDetailAssembler formeDetailAssembler) {
        this.formeRepositoryPort = formeRepositoryPort;
        this.formeDetailAssembler = formeDetailAssembler;
    }

    @Override
    @Transactional
    public FormeDetail desarchiver(DesarchiveFormeCommand command) {
        Forme forme = formeRepositoryPort.findById(FormeId.of(command.formeId()))
                .orElseThrow(FormeIntrouvableException::new);

        forme.desarchiver();

        Forme saved = formeRepositoryPort.save(forme);
        return formeDetailAssembler.assembler(saved);
    }
}
