package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.FamilleDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.exception.CodeFamilleDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.in.CreateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFamilleUseCaseImpl implements CreateFamilleUseCase {

    private final FamilleRepositoryPort familleRepositoryPort;
    private final FamilleDetailAssembler familleDetailAssembler;

    public CreateFamilleUseCaseImpl(FamilleRepositoryPort familleRepositoryPort,
            FamilleDetailAssembler familleDetailAssembler) {
        this.familleRepositoryPort = familleRepositoryPort;
        this.familleDetailAssembler = familleDetailAssembler;
    }

    @Override
    @Transactional
    public FamilleDetail creer(CreateFamilleCommand command) {
        if (familleRepositoryPort.existsByCodeIgnoreCase(command.code().trim())) {
            throw new CodeFamilleDejaUtiliseException(command.code());
        }

        Famille famille = Famille.creer(command.code(), command.libelle(), command.description());

        Famille saved = familleRepositoryPort.save(famille);
        return familleDetailAssembler.assembler(saved);
    }
}
