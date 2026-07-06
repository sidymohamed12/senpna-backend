package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ArchiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.in.ArchiverProjetUseCase;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiverProjetUseCaseImpl implements ArchiverProjetUseCase {

    private final ProjetRepositoryPort projetRepositoryPort;
    private final ProjetDetailAssembler assembler;

    public ArchiverProjetUseCaseImpl(ProjetRepositoryPort projetRepositoryPort, ProjetDetailAssembler assembler) {
        this.projetRepositoryPort = projetRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ProjetDetail archiver(ArchiverProjetCommand command) {
        Projet projet = projetRepositoryPort.findById(ProjetId.of(command.projetId()))
                .orElseThrow(ProjetIntrouvableException::new);

        projet.archiver();

        Projet saved = projetRepositoryPort.save(projet);
        return assembler.assembler(saved);
    }
}
