package ministere.sante.senpna.stock.domain.port.in.stock;

import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;

public interface ListStocksUseCase {
    StockPage lister(ListStocksQuery query);
}
