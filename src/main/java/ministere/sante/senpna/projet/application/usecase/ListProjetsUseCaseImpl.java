package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetCommandMapper;
import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ListProjetsQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetPage;
import ministere.sante.senpna.projet.domain.criteria.ProjetSearchCriteria;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.in.ListProjetsUseCase;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListProjetsUseCaseImpl implements ListProjetsUseCase {

    private final ProjetRepositoryPort projetRepositoryPort;
    private final ProjetCommandMapper commandMapper;
    private final ProjetDetailAssembler assembler;

    public ListProjetsUseCaseImpl(ProjetRepositoryPort projetRepositoryPort, ProjetCommandMapper commandMapper,
            ProjetDetailAssembler assembler) {
        this.projetRepositoryPort = projetRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjetPage lister(ListProjetsQuery query) {
        CategorieProjet categorie = commandMapper.versCategorieOptionnelle(query.categorie());
        StatutProjet statut = commandMapper.versStatutOptionnel(query.statut());
        ProjetSearchCriteria criteria = new ProjetSearchCriteria(query.recherche(), categorie, statut);
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Projet> result = projetRepositoryPort.search(criteria, pageRequest);

        return new ProjetPage(
                result.content().stream().map(assembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
