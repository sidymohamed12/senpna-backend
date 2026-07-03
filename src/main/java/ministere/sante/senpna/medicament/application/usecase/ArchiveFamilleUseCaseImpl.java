package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.FamilleDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.exception.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.in.ArchiveFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveFamilleUseCaseImpl implements ArchiveFamilleUseCase {

    private final FamilleRepositoryPort familleRepositoryPort;
    private final FamilleDetailAssembler familleDetailAssembler;

    public ArchiveFamilleUseCaseImpl(FamilleRepositoryPort familleRepositoryPort,
            FamilleDetailAssembler familleDetailAssembler) {
        this.familleRepositoryPort = familleRepositoryPort;
        this.familleDetailAssembler = familleDetailAssembler;
    }

    @Override
    @Transactional
    public FamilleDetail archiver(ArchiveFamilleCommand command) {
        Famille famille = familleRepositoryPort.findById(FamilleId.of(command.familleId()))
                .orElseThrow(FamilleIntrouvableException::new);

        famille.archiver();

        Famille saved = familleRepositoryPort.save(famille);
        return familleDetailAssembler.assembler(saved);
    }
}
