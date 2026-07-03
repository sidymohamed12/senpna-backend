package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.MedicamentDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.exception.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.in.ArchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveMedicamentUseCaseImpl implements ArchiveMedicamentUseCase {

    private final MedicamentRepositoryPort medicamentRepositoryPort;
    private final MedicamentDetailAssembler medicamentDetailAssembler;

    public ArchiveMedicamentUseCaseImpl(MedicamentRepositoryPort medicamentRepositoryPort,
            MedicamentDetailAssembler medicamentDetailAssembler) {
        this.medicamentRepositoryPort = medicamentRepositoryPort;
        this.medicamentDetailAssembler = medicamentDetailAssembler;
    }

    @Override
    @Transactional
    public MedicamentDetail archiver(ArchiveMedicamentCommand command) {
        Medicament medicament = medicamentRepositoryPort.findById(MedicamentId.of(command.medicamentId()))
                .orElseThrow(MedicamentIntrouvableException::new);

        medicament.archiver();

        Medicament saved = medicamentRepositoryPort.save(medicament);
        return medicamentDetailAssembler.assembler(saved);
    }
}
