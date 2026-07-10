package ministere.sante.senpna.medicament.application.usecase.medicament;

import ministere.sante.senpna.medicament.application.service.MedicamentDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleInactiveException;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeInactiveException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.medicament.CodeMedicamentDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.in.medicament.CreateMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateMedicamentUseCaseImpl implements CreateMedicamentUseCase {

    private final MedicamentRepositoryPort medicamentRepositoryPort;
    private final FamilleRepositoryPort familleRepositoryPort;
    private final FormeRepositoryPort formeRepositoryPort;
    private final MedicamentDetailAssembler medicamentDetailAssembler;

    public CreateMedicamentUseCaseImpl(MedicamentRepositoryPort medicamentRepositoryPort,
            FamilleRepositoryPort familleRepositoryPort, FormeRepositoryPort formeRepositoryPort,
            MedicamentDetailAssembler medicamentDetailAssembler) {
        this.medicamentRepositoryPort = medicamentRepositoryPort;
        this.familleRepositoryPort = familleRepositoryPort;
        this.formeRepositoryPort = formeRepositoryPort;
        this.medicamentDetailAssembler = medicamentDetailAssembler;
    }

    @Override
    @Transactional
    public MedicamentDetail creer(CreateMedicamentCommand command) {
        if (medicamentRepositoryPort.existsByCodeIgnoreCase(command.code().trim())) {
            throw new CodeMedicamentDejaUtiliseException(command.code());
        }

        Famille famille = familleRepositoryPort.findById(FamilleId.of(command.familleId()))
                .orElseThrow(FamilleIntrouvableException::new);
        if (!famille.isActif()) {
            throw new FamilleInactiveException();
        }

        Forme forme = formeRepositoryPort.findById(FormeId.of(command.formeId()))
                .orElseThrow(FormeIntrouvableException::new);
        if (!forme.isActif()) {
            throw new FormeInactiveException();
        }

        Medicament medicament = Medicament.creer(command.code(), command.nomCommercial(), command.dci(),
                command.dosage(), forme.getId(), famille.getId(), command.voieAdministration(),
                command.temperatureConservation(), command.programmeSante(),
                command.delaiApprovisionnementJours(), command.necessiteOrdonnance(), command.fabricant(),
                command.stockMinimum(), command.stockMaximum());

        Medicament saved = medicamentRepositoryPort.save(medicament);
        return medicamentDetailAssembler.assembler(saved);
    }
}
