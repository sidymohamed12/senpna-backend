package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.FamilleDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetFamilleQuery;
import ministere.sante.senpna.medicament.domain.exception.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.in.GetFamilleUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetFamilleUseCaseImpl implements GetFamilleUseCase {

    private final FamilleRepositoryPort familleRepositoryPort;
    private final FamilleDetailAssembler familleDetailAssembler;

    public GetFamilleUseCaseImpl(FamilleRepositoryPort familleRepositoryPort,
            FamilleDetailAssembler familleDetailAssembler) {
        this.familleRepositoryPort = familleRepositoryPort;
        this.familleDetailAssembler = familleDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FamilleDetail obtenir(GetFamilleQuery query) {
        Famille famille = familleRepositoryPort.findById(FamilleId.of(query.familleId()))
                .orElseThrow(FamilleIntrouvableException::new);
        return familleDetailAssembler.assembler(famille);
    }
}
