package ministere.sante.senpna.stock.domain.port.in.lot;

import ministere.sante.senpna.stock.domain.command.LotCommands.ListLotsQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;

public interface ListLotsUseCase {
    LotPage lister(ListLotsQuery query);
}
