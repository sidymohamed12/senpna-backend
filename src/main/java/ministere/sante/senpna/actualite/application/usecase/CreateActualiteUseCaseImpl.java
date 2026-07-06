package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteCommandMapper;
import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.CreateActualiteCommand;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.port.in.CreateActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreateActualiteUseCaseImpl implements CreateActualiteUseCase {

    private final ActualiteRepositoryPort actualiteRepositoryPort;
    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final ActualiteCommandMapper commandMapper;
    private final ActualiteDetailAssembler assembler;

    public CreateActualiteUseCaseImpl(ActualiteRepositoryPort actualiteRepositoryPort,
            UserManagementRepositoryPort userManagementRepositoryPort, ActualiteCommandMapper commandMapper,
            ActualiteDetailAssembler assembler) {
        this.actualiteRepositoryPort = actualiteRepositoryPort;
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ActualiteDetail creer(CreateActualiteCommand command) {
        User auteur = userManagementRepositoryPort.findById(UserId.of(command.auteurId()))
                .orElseThrow(UserNotFoundException::new);

        CategorieActualite categorie = commandMapper.versCategorie(command.categorie());
        List<ActualiteMedia> medias = commandMapper.versMedias(command.medias());
        String auteurNom = auteur.getPrenom().getValue() + " " + auteur.getNom().getValue();

        Actualite actualite = Actualite.creer(categorie, command.titre(), command.description(), medias,
                command.auteurId(), auteurNom, command.tags());

        Actualite saved = actualiteRepositoryPort.save(actualite);

        return assembler.assembler(saved);
    }
}
