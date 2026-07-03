package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.FamilleDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFamillesQuery;
import ministere.sante.senpna.medicament.domain.criteria.FamilleSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.port.in.ListFamillesUseCase;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListFamillesUseCaseImpl implements ListFamillesUseCase {

    private final FamilleRepositoryPort familleRepositoryPort;
    private final FamilleDetailAssembler familleDetailAssembler;

    public ListFamillesUseCaseImpl(FamilleRepositoryPort familleRepositoryPort,
            FamilleDetailAssembler familleDetailAssembler) {
        this.familleRepositoryPort = familleRepositoryPort;
        this.familleDetailAssembler = familleDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public FamillePage lister(ListFamillesQuery query) {
        FamilleSearchCriteria criteria = new FamilleSearchCriteria(query.recherche(), query.actif());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Famille> result = familleRepositoryPort.search(criteria, pageRequest);

        return new FamillePage(
                result.content().stream().map(familleDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
