package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereCommandMapper;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.ListOpportunitesCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarrierePage;
import ministere.sante.senpna.carriere.domain.criteria.OpportuniteCarriereSearchCriteria;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.in.ListOpportunitesCarriereUseCase;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListOpportunitesCarriereUseCaseImpl implements ListOpportunitesCarriereUseCase {

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
    private final OpportuniteCarriereCommandMapper commandMapper;
    private final OpportuniteCarriereDetailAssembler assembler;

    public ListOpportunitesCarriereUseCaseImpl(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort,
            OpportuniteCarriereCommandMapper commandMapper, OpportuniteCarriereDetailAssembler assembler) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
        this.commandMapper = commandMapper;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public OpportuniteCarrierePage lister(ListOpportunitesCarriereQuery query) {
        TypeContrat typeContrat = commandMapper.versTypeContratOptionnel(query.typeContrat());
        StatutOpportunite statut = commandMapper.versStatutOptionnel(query.statut());
        OpportuniteCarriereSearchCriteria criteria = new OpportuniteCarriereSearchCriteria(
                query.recherche(), typeContrat, statut, query.publicOnly());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<OpportuniteCarriere> result = opportuniteCarriereRepositoryPort.search(criteria, pageRequest);

        return new OpportuniteCarrierePage(
                result.content().stream().map(assembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
