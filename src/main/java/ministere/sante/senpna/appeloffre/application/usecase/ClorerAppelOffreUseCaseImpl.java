package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ClorerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.in.ClorerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClorerAppelOffreUseCaseImpl implements ClorerAppelOffreUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;
    private final AppelOffreDetailAssembler appelOffreDetailAssembler;

    public ClorerAppelOffreUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort,
            AppelOffreDetailAssembler appelOffreDetailAssembler) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
        this.appelOffreDetailAssembler = appelOffreDetailAssembler;
    }

    @Override
    @Transactional
    public AppelOffreDetail clorer(ClorerAppelOffreCommand command) {
        AppelOffre appelOffre = appelOffreRepositoryPort.findById(AppelOffreId.of(command.appelOffreId()))
                .orElseThrow(AppelOffreIntrouvableException::new);

        appelOffre.cloturer();

        AppelOffre saved = appelOffreRepositoryPort.save(appelOffre);
        return appelOffreDetailAssembler.assembler(saved);
    }
}
