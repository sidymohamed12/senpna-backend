package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.CreateAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.LigneAppelOffreInput;
import ministere.sante.senpna.appeloffre.domain.exception.ReferenceAppelOffreDejaUtiliseeException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.in.CreateAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAppelOffreUseCaseImpl implements CreateAppelOffreUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;
    private final AppelOffreDetailAssembler appelOffreDetailAssembler;

    public CreateAppelOffreUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort,
            AppelOffreDetailAssembler appelOffreDetailAssembler) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
        this.appelOffreDetailAssembler = appelOffreDetailAssembler;
    }

    @Override
    @Transactional
    public AppelOffreDetail creer(CreateAppelOffreCommand command) {
        if (appelOffreRepositoryPort.existsByReferenceIgnoreCase(command.reference().trim())) {
            throw new ReferenceAppelOffreDejaUtiliseeException(command.reference());
        }

        java.util.List<LigneAppelOffre> lignes = command.lignes().stream().map(this::toLigne).toList();

        AppelOffre appelOffre = AppelOffre.creer(new AppelOffre.CreationCommand(command.reference(), command.objet(), command.dateCloture(),
                lignes));

        AppelOffre saved = appelOffreRepositoryPort.save(appelOffre);
        return appelOffreDetailAssembler.assembler(saved);
    }

    private LigneAppelOffre toLigne(LigneAppelOffreInput input) {
        return LigneAppelOffre.creer(MedicamentId.of(input.medicamentId()), input.designation(),
                input.quantiteEstimee(), input.uniteBase());
    }
}
