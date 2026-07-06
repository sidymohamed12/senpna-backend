package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteCommandMapper;
import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualitePage;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ListActualitesQuery;
import ministere.sante.senpna.actualite.domain.criteria.ActualiteSearchCriteria;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.in.ListActualitesUseCase;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListActualitesUseCaseImpl implements ListActualitesUseCase {

    private final ActualiteRepositoryPort actualiteRepositoryPort;
    private final ActualiteCommandMapper commandMapper;
    private final ActualiteDetailAssembler assembler;

    public ListActualitesUseCaseImpl(ActualiteRepositoryPort actualiteRepositoryPort,
            ActualiteCommandMapper commandMapper, ActualiteDetailAssembler assembler) {
        this.actualiteRepositoryPort = actualiteRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ActualitePage lister(ListActualitesQuery query) {
        CategorieActualite categorie = commandMapper.versCategorieOptionnelle(query.categorie());
        StatutActualite statut = commandMapper.versStatutOptionnel(query.statut());
        ActualiteSearchCriteria criteria = new ActualiteSearchCriteria(query.recherche(), categorie, statut);
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Actualite> result = actualiteRepositoryPort.search(criteria, pageRequest);

        return new ActualitePage(
                result.content().stream().map(assembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
