package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.StockCommands.GetStockQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;

public interface GetStockUseCase {
    StockDetail obtenir(GetStockQuery query);
}
