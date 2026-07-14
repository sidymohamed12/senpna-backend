package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.in.GetAppelOffrePublieUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAppelOffrePublieUseCaseImpl implements GetAppelOffrePublieUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;
    private final AppelOffreDetailAssembler appelOffreDetailAssembler;

    public GetAppelOffrePublieUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort,
            AppelOffreDetailAssembler appelOffreDetailAssembler) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
        this.appelOffreDetailAssembler = appelOffreDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public AppelOffreDetail obtenir(GetAppelOffreQuery query) {
        AppelOffre appelOffre = appelOffreRepositoryPort.findById(AppelOffreId.of(query.appelOffreId()))
                .orElseThrow(AppelOffreIntrouvableException::new);

        // Un AO en brouillon n'a jamais existé pour le fournisseur — même
        // message d'erreur qu'un identifiant inconnu, pour ne pas fuiter
        // d'information sur les AO en préparation.
        if (appelOffre.getStatut() == StatutAppelOffre.BROUILLON) {
            throw new AppelOffreIntrouvableException();
        }

        return appelOffreDetailAssembler.assembler(appelOffre);
    }
}
