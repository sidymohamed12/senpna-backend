package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.domain.command.LotCommands.ListLotsQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.domain.criteria.LotSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.in.ListLotsUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListLotsUseCaseImpl implements ListLotsUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final LotDetailAssembler lotDetailAssembler;

    public ListLotsUseCaseImpl(LotRepositoryPort lotRepositoryPort, LotDetailAssembler lotDetailAssembler) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.lotDetailAssembler = lotDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public LotPage lister(ListLotsQuery query) {
        LotSearchCriteria criteria = new LotSearchCriteria(
                query.recherche(),
                query.medicamentId(),
                query.fournisseurId(),
                parseStatut(query.statut()));
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Lot> result = lotRepositoryPort.search(criteria, pageRequest);

        return new LotPage(
                result.content().stream().map(lotDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    private static StatutLot parseStatut(String statut) {
        if (statut == null || statut.isBlank()) {
            return null;
        }
        try {
            return StatutLot.valueOf(statut.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Statut de lot invalide : " + statut, "LOT_STATUT_INVALID");
        }
    }
}
