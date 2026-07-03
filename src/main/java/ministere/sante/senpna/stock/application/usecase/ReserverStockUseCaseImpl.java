package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReserverStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.exception.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.exception.LotNonDisponibleException;
import ministere.sante.senpna.stock.domain.exception.StockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.ReserverStockUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Réservation manuelle ciblée sur une ligne de stock (entrepôt, lot) déjà
 * identifiée — utilisée lorsque le lot a été sélectionné explicitement
 * (ex : par un pharmacien lors du contrôle des lots), par opposition à la
 * réservation automatique FEFO ({@code ReserverStockFefoUseCase}).
 */
@Service
public class ReserverStockUseCaseImpl implements ReserverStockUseCase {

    private final StockRepositoryPort stockRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public ReserverStockUseCaseImpl(StockRepositoryPort stockRepositoryPort, LotRepositoryPort lotRepositoryPort,
            StockDetailAssembler stockDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional
    public StockDetail reserver(ReserverStockCommand command) {
        entrepotScopeGuard.verifierEcritureAutorisee(command.entrepotId());

        Lot lot = lotRepositoryPort.findById(LotId.of(command.lotId())).orElseThrow(LotIntrouvableException::new);

        if (!lot.peutEtreReserveOuExpedie()) {
            throw new LotNonDisponibleException(lot.getNumeroLot());
        }

        Stock stock = stockRepositoryPort
                .findByEntrepotIdAndLotIdForUpdate(EntrepotId.of(command.entrepotId()), lot.getId())
                .orElseThrow(StockIntrouvableException::new);

        stock.reserver(command.quantite());
        Stock saved = stockRepositoryPort.save(stock);

        return stockDetailAssembler.assembler(saved);
    }
}
