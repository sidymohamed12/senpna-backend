package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.CandidatureDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidaturePage;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.ListCandidaturesQuery;
import ministere.sante.senpna.carriere.domain.criteria.CandidatureSearchCriteria;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.port.in.ListCandidaturesUseCase;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCandidaturesUseCaseImpl implements ListCandidaturesUseCase {

    private final CandidatureRepositoryPort candidatureRepositoryPort;
    private final CandidatureDetailAssembler assembler;

    public ListCandidaturesUseCaseImpl(CandidatureRepositoryPort candidatureRepositoryPort,
            CandidatureDetailAssembler assembler) {
        this.candidatureRepositoryPort = candidatureRepositoryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CandidaturePage lister(ListCandidaturesQuery query) {
        CandidatureSearchCriteria criteria = new CandidatureSearchCriteria(query.opportuniteId(), query.recherche());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Candidature> result = candidatureRepositoryPort.search(criteria, pageRequest);

        return new CandidaturePage(
                result.content().stream().map(assembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
