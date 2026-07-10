package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;
import ministere.sante.senpna.stock.domain.criteria.StockSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.stock.ListerAlertesRuptureUseCase;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Alertes de rupture et de seuil minimum (cf. doc. métier §16 et §17) :
 * remonte les lignes de stock en rupture (quantité disponible à la vente
 * ≤ 0), ou ayant atteint leur seuil d'alerte si {@code seuilAtteintUniquement}
 * est demandé explicitement — sinon la rupture prévaut par défaut.
 */
@Service
public class ListerAlertesRuptureUseCaseImpl implements ListerAlertesRuptureUseCase {

    private final StockRepositoryPort stockRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public ListerAlertesRuptureUseCaseImpl(StockRepositoryPort stockRepositoryPort,
            StockDetailAssembler stockDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public StockPage lister(ListStocksQuery query) {
        boolean seuilAtteintUniquement = Boolean.TRUE.equals(query.seuilAtteintUniquement());
        UUID entrepotIdEffectif = entrepotScopeGuard.entrepotIdPourLecture(query.entrepotId());

        StockSearchCriteria criteria = new StockSearchCriteria(
                entrepotIdEffectif,
                query.lotId(),
                query.medicamentId(),
                seuilAtteintUniquement ? null : Boolean.TRUE,
                seuilAtteintUniquement ? Boolean.TRUE : null);
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), "quantiteDisponible", "ASC");

        PageResult<Stock> result = stockRepositoryPort.search(criteria, pageRequest);

        return new StockPage(
                result.content().stream().map(stockDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
