package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListStructuresSanitairesQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitairePage;
import ministere.sante.senpna.organisation.domain.criteria.StructureSanitaireSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.in.ListStructuresSanitairesUseCase;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListStructuresSanitairesUseCaseImpl implements ListStructuresSanitairesUseCase {

    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
    private final StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;

    public ListStructuresSanitairesUseCaseImpl(StructureSanitaireRepositoryPort structureSanitaireRepositoryPort,
            StructureSanitaireDetailAssembler structureSanitaireDetailAssembler) {
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
        this.structureSanitaireDetailAssembler = structureSanitaireDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public StructureSanitairePage lister(ListStructuresSanitairesQuery query) {
        StructureSanitaireSearchCriteria criteria = new StructureSanitaireSearchCriteria(query.recherche(),
                query.type(), query.regionId(), query.praId(), query.statutAdhesion(), query.actif());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<StructureSanitaire> result = structureSanitaireRepositoryPort.search(criteria, pageRequest);

        return new StructureSanitairePage(
                result.content().stream().map(structureSanitaireDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
