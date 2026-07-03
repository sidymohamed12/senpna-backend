package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.MedicamentDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.exception.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.in.DesarchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesarchiveMedicamentUseCaseImpl implements DesarchiveMedicamentUseCase {

    private final MedicamentRepositoryPort medicamentRepositoryPort;
    private final MedicamentDetailAssembler medicamentDetailAssembler;

    public DesarchiveMedicamentUseCaseImpl(MedicamentRepositoryPort medicamentRepositoryPort,
            MedicamentDetailAssembler medicamentDetailAssembler) {
        this.medicamentRepositoryPort = medicamentRepositoryPort;
        this.medicamentDetailAssembler = medicamentDetailAssembler;
    }

    @Override
    @Transactional
    public MedicamentDetail desarchiver(DesarchiveMedicamentCommand command) {
        Medicament medicament = medicamentRepositoryPort.findById(MedicamentId.of(command.medicamentId()))
                .orElseThrow(MedicamentIntrouvableException::new);

        medicament.desarchiver();

        Medicament saved = medicamentRepositoryPort.save(medicament);
        return medicamentDetailAssembler.assembler(saved);
    }
}
