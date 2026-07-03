package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.LotCommands.AlertePeremptionQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;

public interface ListerAlertesPeremptionUseCase {
    LotPage lister(AlertePeremptionQuery query);
}
