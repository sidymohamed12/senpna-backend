package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetCommandMapper;
import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.CreateProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.in.CreateProjetUseCase;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProjetUseCaseImpl implements CreateProjetUseCase {

    private final ProjetRepositoryPort projetRepositoryPort;
    private final ProjetCommandMapper commandMapper;
    private final ProjetDetailAssembler assembler;

    public CreateProjetUseCaseImpl(ProjetRepositoryPort projetRepositoryPort, ProjetCommandMapper commandMapper,
            ProjetDetailAssembler assembler) {
        this.projetRepositoryPort = projetRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ProjetDetail creer(CreateProjetCommand command) {
        CategorieProjet categorie = commandMapper.versCategorie(command.categorie());

        Projet projet = Projet.creer(new Projet.CreationCommand(categorie, command.nom(), command.description(), command.objectifs(),
                command.impacts(), command.imageUrl()));

        Projet saved = projetRepositoryPort.save(projet);
        return assembler.assembler(saved);
    }
}
