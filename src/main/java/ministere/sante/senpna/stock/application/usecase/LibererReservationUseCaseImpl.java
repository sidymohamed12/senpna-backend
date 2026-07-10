package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.LibererReservationCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.exception.stock.StockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.stock.LibererReservationUseCase;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Libère tout ou partie d'une réservation (commande annulée, rejetée, ou
 * quantité revue à la baisse — cf. doc. métier §10).
 */
@Service
public class LibererReservationUseCaseImpl implements LibererReservationUseCase {

    private final StockRepositoryPort stockRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public LibererReservationUseCaseImpl(StockRepositoryPort stockRepositoryPort,
            StockDetailAssembler stockDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional
    public StockDetail liberer(LibererReservationCommand command) {
        entrepotScopeGuard.verifierEcritureAutorisee(command.entrepotId());

        Stock stock = stockRepositoryPort
                .findByEntrepotIdAndLotIdForUpdate(EntrepotId.of(command.entrepotId()), LotId.of(command.lotId()))
                .orElseThrow(StockIntrouvableException::new);

        stock.libererReservation(command.quantite());
        Stock saved = stockRepositoryPort.save(stock);

        return stockDetailAssembler.assembler(saved);
    }
}
