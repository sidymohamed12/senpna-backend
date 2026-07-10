package ministere.sante.senpna.organisation.application.usecase.entrepot;

import ministere.sante.senpna.organisation.application.service.EntrepotDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ListEntrepotsQuery;
import ministere.sante.senpna.organisation.domain.criteria.EntrepotSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.in.entrepot.ListEntrepotsUseCase;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListEntrepotsUseCaseImpl implements ListEntrepotsUseCase {

    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final EntrepotDetailAssembler entrepotDetailAssembler;

    public ListEntrepotsUseCaseImpl(EntrepotRepositoryPort entrepotRepositoryPort,
            EntrepotDetailAssembler entrepotDetailAssembler) {
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.entrepotDetailAssembler = entrepotDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public EntrepotPage lister(ListEntrepotsQuery query) {
        EntrepotSearchCriteria criteria = new EntrepotSearchCriteria(query.recherche(), query.type(),
                query.regionId(), query.actif());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Entrepot> result = entrepotRepositoryPort.search(criteria, pageRequest);

        return new EntrepotPage(
                result.content().stream().map(entrepotDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
