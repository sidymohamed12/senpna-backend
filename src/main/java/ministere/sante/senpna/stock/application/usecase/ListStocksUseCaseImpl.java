package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.ListStocksUseCase;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    private final EntrepotScopeGuard entrepotScopeGuard;

    public ListStocksUseCaseImpl(StockRepositoryPort stockRepositoryPort,
            StockDetailAssembler stockDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public StockPage lister(ListStocksQuery query) {
        // PRA : toujours ramené à son propre entrepôt (le filtre demandé, s'il
        // en désignait un autre, est ignoré). PNA : filtre libre, y compris
        // aucun (vision globale sur tous les entrepôts).
        UUID entrepotIdEffectif = entrepotScopeGuard.entrepotIdPourLecture(query.entrepotId());

        StockSearchCriteria criteria = new StockSearchCriteria(
                entrepotIdEffectif,
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
