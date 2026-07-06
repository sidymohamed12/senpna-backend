package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.RemettreEnBrouillonProjetCommand;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.in.RemettreEnBrouillonProjetUseCase;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemettreEnBrouillonProjetUseCaseImpl implements RemettreEnBrouillonProjetUseCase {

    private final ProjetRepositoryPort projetRepositoryPort;
    private final ProjetDetailAssembler assembler;

    public RemettreEnBrouillonProjetUseCaseImpl(ProjetRepositoryPort projetRepositoryPort,
            ProjetDetailAssembler assembler) {
        this.projetRepositoryPort = projetRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ProjetDetail remettreEnBrouillon(RemettreEnBrouillonProjetCommand command) {
        Projet projet = projetRepositoryPort.findById(ProjetId.of(command.projetId()))
                .orElseThrow(ProjetIntrouvableException::new);

        projet.remettreEnBrouillon();

        Projet saved = projetRepositoryPort.save(projet);
        return assembler.assembler(saved);
    }
}
