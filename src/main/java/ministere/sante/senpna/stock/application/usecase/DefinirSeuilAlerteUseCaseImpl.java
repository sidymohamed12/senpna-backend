package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.DefinirSeuilAlerteCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.exception.stock.StockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.DefinirSeuilAlerteUseCase;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.StockId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefinirSeuilAlerteUseCaseImpl implements DefinirSeuilAlerteUseCase {

    private final StockRepositoryPort stockRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public DefinirSeuilAlerteUseCaseImpl(StockRepositoryPort stockRepositoryPort,
            StockDetailAssembler stockDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional
    public StockDetail definir(DefinirSeuilAlerteCommand command) {
        Stock stock = stockRepositoryPort.findById(StockId.of(command.stockId()))
                .orElseThrow(StockIntrouvableException::new);

        // Le stockId ne révèle pas son entrepôt tant qu'on ne l'a pas chargé :
        // la vérification de portée n'intervient donc qu'à ce stade.
        entrepotScopeGuard.verifierEcritureAutorisee(stock.getEntrepotId().getValue());

        stock.definirSeuilAlerte(command.seuilAlerte());

        Stock saved = stockRepositoryPort.save(stock);
        return stockDetailAssembler.assembler(saved);
    }
}
