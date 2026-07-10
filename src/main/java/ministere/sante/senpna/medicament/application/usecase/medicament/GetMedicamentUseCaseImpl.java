package ministere.sante.senpna.medicament.application.usecase.medicament;

import ministere.sante.senpna.medicament.application.service.MedicamentDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.in.medicament.GetMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMedicamentUseCaseImpl implements GetMedicamentUseCase {

    private final MedicamentRepositoryPort medicamentRepositoryPort;
    private final MedicamentDetailAssembler medicamentDetailAssembler;

    public GetMedicamentUseCaseImpl(MedicamentRepositoryPort medicamentRepositoryPort,
            MedicamentDetailAssembler medicamentDetailAssembler) {
        this.medicamentRepositoryPort = medicamentRepositoryPort;
        this.medicamentDetailAssembler = medicamentDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public MedicamentDetail obtenir(GetMedicamentQuery query) {
        Medicament medicament = medicamentRepositoryPort.findById(MedicamentId.of(query.medicamentId()))
                .orElseThrow(MedicamentIntrouvableException::new);
        return medicamentDetailAssembler.assembler(medicament);
    }
}
