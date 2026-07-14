package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.in.ClorerAppelOffresExpiresUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClorerAppelOffresExpiresUseCaseImpl implements ClorerAppelOffresExpiresUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;

    public ClorerAppelOffresExpiresUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
    }

    @Override
    @Transactional
    public int clorerExpires() {
        List<AppelOffre> expires = appelOffreRepositoryPort.findPubliesAvecClotureDepassee();
        for (AppelOffre appelOffre : expires) {
            appelOffre.cloturer();
            appelOffreRepositoryPort.save(appelOffre);
        }
        return expires.size();
    }
}
