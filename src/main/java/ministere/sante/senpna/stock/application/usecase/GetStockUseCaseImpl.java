package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.GetStockQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.exception.StockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.GetStockUseCase;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.StockId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetStockUseCaseImpl implements GetStockUseCase {

    private final StockRepositoryPort stockRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;

    public GetStockUseCaseImpl(StockRepositoryPort stockRepositoryPort, StockDetailAssembler stockDetailAssembler) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public StockDetail obtenir(GetStockQuery query) {
        Stock stock = stockRepositoryPort.findById(StockId.of(query.stockId()))
                .orElseThrow(StockIntrouvableException::new);
        return stockDetailAssembler.assembler(stock);
    }
}
