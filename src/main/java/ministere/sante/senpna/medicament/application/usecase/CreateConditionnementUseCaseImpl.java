package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.exception.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.NiveauConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.NomConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.UniteBaseDejaDefinieException;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.in.CreateConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateConditionnementUseCaseImpl implements CreateConditionnementUseCase {

    private final ConditionnementRepositoryPort conditionnementRepositoryPort;
    private final MedicamentRepositoryPort medicamentRepositoryPort;
    private final ConditionnementDetailAssembler conditionnementDetailAssembler;

    public CreateConditionnementUseCaseImpl(ConditionnementRepositoryPort conditionnementRepositoryPort,
            MedicamentRepositoryPort medicamentRepositoryPort,
            ConditionnementDetailAssembler conditionnementDetailAssembler) {
        this.conditionnementRepositoryPort = conditionnementRepositoryPort;
        this.medicamentRepositoryPort = medicamentRepositoryPort;
        this.conditionnementDetailAssembler = conditionnementDetailAssembler;
    }

    @Override
    @Transactional
    public ConditionnementDetail creer(CreateConditionnementCommand command) {
        Medicament medicament = medicamentRepositoryPort.findById(MedicamentId.of(command.medicamentId()))
                .orElseThrow(MedicamentIntrouvableException::new);

        if (conditionnementRepositoryPort.existsByMedicamentIdAndNiveau(medicament.getId(), command.niveau())) {
            throw new NiveauConditionnementDejaUtiliseException(command.niveau());
        }
        if (conditionnementRepositoryPort.existsByMedicamentIdAndNomIgnoreCase(medicament.getId(),
                command.nom().trim())) {
            throw new NomConditionnementDejaUtiliseException(command.nom());
        }
        if (command.estUniteBase()
                && conditionnementRepositoryPort.existsUniteBaseByMedicamentId(medicament.getId())) {
            throw new UniteBaseDejaDefinieException();
        }

        Conditionnement conditionnement = Conditionnement.creer(medicament.getId(), command.nom(), command.niveau(),
                command.quantiteUniteBase(), command.estUniteBase());

        Conditionnement saved = conditionnementRepositoryPort.save(conditionnement);
        return conditionnementDetailAssembler.assembler(saved);
    }
}
