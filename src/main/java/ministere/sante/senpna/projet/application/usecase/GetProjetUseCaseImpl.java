package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.GetProjetQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.in.GetProjetUseCase;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetProjetUseCaseImpl implements GetProjetUseCase {

    private final ProjetRepositoryPort projetRepositoryPort;
    private final ProjetDetailAssembler assembler;

    public GetProjetUseCaseImpl(ProjetRepositoryPort projetRepositoryPort, ProjetDetailAssembler assembler) {
        this.projetRepositoryPort = projetRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjetDetail obtenir(GetProjetQuery query) {
        Projet projet = projetRepositoryPort.findById(ProjetId.of(query.projetId()))
                .orElseThrow(ProjetIntrouvableException::new);
        return assembler.assembler(projet);
    }
}
