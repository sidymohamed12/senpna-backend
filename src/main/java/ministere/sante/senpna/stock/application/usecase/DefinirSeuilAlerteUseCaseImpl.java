package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.DefinirSeuilAlerteCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.exception.StockIntrouvableException;
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

    public DefinirSeuilAlerteUseCaseImpl(StockRepositoryPort stockRepositoryPort,
            StockDetailAssembler stockDetailAssembler) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
    }

    @Override
    @Transactional
    public StockDetail definir(DefinirSeuilAlerteCommand command) {
        Stock stock = stockRepositoryPort.findById(StockId.of(command.stockId()))
                .orElseThrow(StockIntrouvableException::new);

        stock.definirSeuilAlerte(command.seuilAlerte());

        Stock saved = stockRepositoryPort.save(stock);
        return stockDetailAssembler.assembler(saved);
    }
}
