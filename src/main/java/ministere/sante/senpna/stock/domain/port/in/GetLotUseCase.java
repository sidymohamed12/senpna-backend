package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.LotCommands.GetLotQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;

public interface GetLotUseCase {
    LotDetail obtenir(GetLotQuery query);
}
