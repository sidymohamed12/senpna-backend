package ministere.sante.senpna.stock.domain.port.in.lot;

import ministere.sante.senpna.stock.domain.command.LotCommands.DebloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;

public interface DebloquerLotUseCase {
    LotDetail debloquer(DebloquerLotCommand command);
}
