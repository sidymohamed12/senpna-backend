package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetCommandMapper;
import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.UpdateProjetCommand;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.in.UpdateProjetUseCase;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProjetUseCaseImpl implements UpdateProjetUseCase {

    private final ProjetRepositoryPort projetRepositoryPort;
    private final ProjetCommandMapper commandMapper;
    private final ProjetDetailAssembler assembler;

    public UpdateProjetUseCaseImpl(ProjetRepositoryPort projetRepositoryPort, ProjetCommandMapper commandMapper,
            ProjetDetailAssembler assembler) {
        this.projetRepositoryPort = projetRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ProjetDetail modifier(UpdateProjetCommand command) {
        Projet projet = projetRepositoryPort.findById(ProjetId.of(command.projetId()))
                .orElseThrow(ProjetIntrouvableException::new);

        CategorieProjet categorie = commandMapper.versCategorie(command.categorie());

        projet.modifierContenu(categorie, command.nom(), command.description(), command.objectifs(),
                command.impacts(), command.imageUrl());

        Projet saved = projetRepositoryPort.save(projet);
        return assembler.assembler(saved);
    }
}
