package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.ListStocksUseCase;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultation paginée des lignes de stock (cf. doc. métier §9), avec
 * filtres par entrepôt, lot, médicament, rupture ou seuil d'alerte
 * atteint. Sert également de base aux alertes de rupture (cf. doc. métier
 * §17) via le filtre {@code ruptureUniquement}.
 */
@Service
public class ListStocksUseCaseImpl implements ListStocksUseCase {

    private final StockRepositoryPort stockRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;

    public ListStocksUseCaseImpl(StockRepositoryPort stockRepositoryPort,
            StockDetailAssembler stockDetailAssembler) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public StockPage lister(ListStocksQuery query) {
        StockSearchCriteria criteria = new StockSearchCriteria(
                query.entrepotId(),
                query.lotId(),
                query.medicamentId(),
                query.ruptureUniquement(),
                query.seuilAtteintUniquement());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), query.sortBy(), query.sortDirection());

        PageResult<Stock> result = stockRepositoryPort.search(criteria, pageRequest);

        return new StockPage(
                result.content().stream().map(stockDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
