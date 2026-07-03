package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.FormeDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFormesQuery;
import ministere.sante.senpna.medicament.domain.criteria.FormeSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.port.in.ListFormesUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListFormesUseCaseImpl implements ListFormesUseCase {

    private final FormeRepositoryPort formeRepositoryPort;
    private final FormeDetailAssembler formeDetailAssembler;

    public ListFormesUseCaseImpl(FormeRepositoryPort formeRepositoryPort,
            FormeDetailAssembler formeDetailAssembler) {
        this.formeRepositoryPort = formeRepositoryPort;
        this.formeDetailAssembler = formeDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FormePage lister(ListFormesQuery query) {
        FormeSearchCriteria criteria = new FormeSearchCriteria(query.recherche(), query.actif());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Forme> result = formeRepositoryPort.search(criteria, pageRequest);

        return new FormePage(
                result.content().stream().map(formeDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
