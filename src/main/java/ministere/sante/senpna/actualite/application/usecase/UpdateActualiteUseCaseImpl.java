package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteCommandMapper;
import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.UpdateActualiteCommand;
import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.port.in.UpdateActualiteUseCase;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpdateActualiteUseCaseImpl implements UpdateActualiteUseCase {

    private final ActualiteRepositoryPort actualiteRepositoryPort;
    private final ActualiteCommandMapper commandMapper;
    private final ActualiteDetailAssembler assembler;

    public UpdateActualiteUseCaseImpl(ActualiteRepositoryPort actualiteRepositoryPort,
            ActualiteCommandMapper commandMapper, ActualiteDetailAssembler assembler) {
        this.actualiteRepositoryPort = actualiteRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public ActualiteDetail modifier(UpdateActualiteCommand command) {
        Actualite actualite = actualiteRepositoryPort.findById(ActualiteId.of(command.actualiteId()))
                .orElseThrow(ActualiteIntrouvableException::new);

        CategorieActualite categorie = commandMapper.versCategorie(command.categorie());
        List<ActualiteMedia> medias = commandMapper.versMedias(command.medias());

        actualite.modifierContenu(categorie, command.titre(), command.description(), medias, command.tags());

        Actualite saved = actualiteRepositoryPort.save(actualite);

        return assembler.assembler(saved);
    }
}
