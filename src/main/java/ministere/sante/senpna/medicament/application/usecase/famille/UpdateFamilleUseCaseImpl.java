package ministere.sante.senpna.medicament.application.usecase.famille;

import ministere.sante.senpna.medicament.application.service.FamilleDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.UpdateFamilleCommand;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.in.famille.UpdateFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.shared.domain.port.out.FamilleCachePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateFamilleUseCaseImpl implements UpdateFamilleUseCase {

    private final FamilleRepositoryPort familleRepositoryPort;
    private final FamilleCachePort familleCachePort;
    private final FamilleDetailAssembler familleDetailAssembler;

    public UpdateFamilleUseCaseImpl(FamilleRepositoryPort familleRepositoryPort, FamilleCachePort familleCachePort,
            FamilleDetailAssembler familleDetailAssembler) {
        this.familleRepositoryPort = familleRepositoryPort;
        this.familleCachePort = familleCachePort;
        this.familleDetailAssembler = familleDetailAssembler;
    }

    @Override
    @Transactional
    public FamilleDetail modifier(UpdateFamilleCommand command) {
        Famille famille = familleRepositoryPort.findById(FamilleId.of(command.familleId()))
                .orElseThrow(FamilleIntrouvableException::new);

        famille.modifierInformations(command.libelle(), command.description());

        Famille saved = familleRepositoryPort.save(famille);
        familleCachePort.reload();

        return familleDetailAssembler.assembler(saved);
    }
}
