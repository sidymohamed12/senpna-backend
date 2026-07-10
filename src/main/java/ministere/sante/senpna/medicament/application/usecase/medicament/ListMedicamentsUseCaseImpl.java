package ministere.sante.senpna.medicament.application.usecase.medicament;

import ministere.sante.senpna.medicament.application.service.MedicamentDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.criteria.MedicamentSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.in.medicament.ListMedicamentsUseCase;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListMedicamentsUseCaseImpl implements ListMedicamentsUseCase {

    private final MedicamentRepositoryPort medicamentRepositoryPort;
    private final MedicamentDetailAssembler medicamentDetailAssembler;

    public ListMedicamentsUseCaseImpl(MedicamentRepositoryPort medicamentRepositoryPort,
            MedicamentDetailAssembler medicamentDetailAssembler) {
        this.medicamentRepositoryPort = medicamentRepositoryPort;
        this.medicamentDetailAssembler = medicamentDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public MedicamentPage lister(ListMedicamentsQuery query) {
        MedicamentSearchCriteria criteria = new MedicamentSearchCriteria(query.recherche(), query.familleId(),
                query.formeId(), query.actif());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Medicament> result = medicamentRepositoryPort.search(criteria, pageRequest);

        return new MedicamentPage(
                result.content().stream().map(medicamentDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
