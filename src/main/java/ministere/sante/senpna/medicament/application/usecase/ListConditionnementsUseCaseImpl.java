package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListConditionnementsQuery;
import ministere.sante.senpna.medicament.domain.criteria.ConditionnementSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.port.in.ListConditionnementsUseCase;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListConditionnementsUseCaseImpl implements ListConditionnementsUseCase {

    private final ConditionnementRepositoryPort conditionnementRepositoryPort;
    private final ConditionnementDetailAssembler conditionnementDetailAssembler;

    public ListConditionnementsUseCaseImpl(ConditionnementRepositoryPort conditionnementRepositoryPort,
            ConditionnementDetailAssembler conditionnementDetailAssembler) {
        this.conditionnementRepositoryPort = conditionnementRepositoryPort;
        this.conditionnementDetailAssembler = conditionnementDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public ConditionnementPage lister(ListConditionnementsQuery query) {
        ConditionnementSearchCriteria criteria = new ConditionnementSearchCriteria(query.medicamentId(),
                query.actif());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Conditionnement> result = conditionnementRepositoryPort.search(criteria, pageRequest);

        return new ConditionnementPage(
                result.content().stream().map(conditionnementDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
