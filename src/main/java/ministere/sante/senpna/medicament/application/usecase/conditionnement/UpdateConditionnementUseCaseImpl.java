package ministere.sante.senpna.medicament.application.usecase.conditionnement;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.UpdateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NiveauConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NomConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.UniteBaseDejaDefinieException;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.UpdateConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateConditionnementUseCaseImpl implements UpdateConditionnementUseCase {

    private final ConditionnementRepositoryPort conditionnementRepositoryPort;
    private final ConditionnementDetailAssembler conditionnementDetailAssembler;

    public UpdateConditionnementUseCaseImpl(ConditionnementRepositoryPort conditionnementRepositoryPort,
            ConditionnementDetailAssembler conditionnementDetailAssembler) {
        this.conditionnementRepositoryPort = conditionnementRepositoryPort;
        this.conditionnementDetailAssembler = conditionnementDetailAssembler;
    }

    @Override
    @Transactional
    public ConditionnementDetail modifier(UpdateConditionnementCommand command) {
        Conditionnement conditionnement = conditionnementRepositoryPort
                .findById(ConditionnementId.of(command.conditionnementId()))
                .orElseThrow(ConditionnementIntrouvableException::new);

        if (conditionnementRepositoryPort.existsByMedicamentIdAndNiveauAndIdNot(conditionnement.getMedicamentId(),
                command.niveau(), conditionnement.getId())) {
            throw new NiveauConditionnementDejaUtiliseException(command.niveau());
        }
        if (conditionnementRepositoryPort.existsByMedicamentIdAndNomIgnoreCaseAndIdNot(
                conditionnement.getMedicamentId(), command.nom().trim(), conditionnement.getId())) {
            throw new NomConditionnementDejaUtiliseException(command.nom());
        }
        if (command.estUniteBase() && conditionnementRepositoryPort.existsUniteBaseByMedicamentIdAndIdNot(
                conditionnement.getMedicamentId(), conditionnement.getId())) {
            throw new UniteBaseDejaDefinieException();
        }

        conditionnement.modifierInformations(command.nom(), command.niveau(), command.quantiteUniteBase(),
                command.estUniteBase(), command.prixAchat(), command.prixVente());

        Conditionnement saved = conditionnementRepositoryPort.save(conditionnement);
        return conditionnementDetailAssembler.assembler(saved);
    }
}
