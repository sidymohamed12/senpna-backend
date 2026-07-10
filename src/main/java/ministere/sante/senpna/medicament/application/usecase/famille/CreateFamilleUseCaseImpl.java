package ministere.sante.senpna.medicament.application.usecase.famille;

import ministere.sante.senpna.medicament.application.service.FamilleDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.exception.famille.CodeFamilleDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.in.famille.CreateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.FamilleCachePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFamilleUseCaseImpl implements CreateFamilleUseCase {

    private final FamilleRepositoryPort familleRepositoryPort;
    private final FamilleCachePort familleCachePort;
    private final FamilleDetailAssembler familleDetailAssembler;

    public CreateFamilleUseCaseImpl(FamilleRepositoryPort familleRepositoryPort, FamilleCachePort familleCachePort,
            FamilleDetailAssembler familleDetailAssembler) {
        this.familleRepositoryPort = familleRepositoryPort;
        this.familleCachePort = familleCachePort;
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

        // Recharge le cache immédiatement — même stratégie que
        // CreateFormeUseCaseImpl / CreateRegionUseCaseImpl.
        familleCachePort.reload();

        return familleDetailAssembler.assembler(saved);
    }
}
