package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.DesactiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.in.DesactiverProjetUseCase;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesactiverProjetUseCaseImpl implements DesactiverProjetUseCase {

    private final ProjetRepositoryPort projetRepositoryPort;
    private final ProjetDetailAssembler assembler;

    public DesactiverProjetUseCaseImpl(ProjetRepositoryPort projetRepositoryPort, ProjetDetailAssembler assembler) {
        this.projetRepositoryPort = projetRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ProjetDetail desactiver(DesactiverProjetCommand command) {
        Projet projet = projetRepositoryPort.findById(ProjetId.of(command.projetId()))
                .orElseThrow(ProjetIntrouvableException::new);

        projet.desactiver();

        Projet saved = projetRepositoryPort.save(projet);
        return assembler.assembler(saved);
    }
}
