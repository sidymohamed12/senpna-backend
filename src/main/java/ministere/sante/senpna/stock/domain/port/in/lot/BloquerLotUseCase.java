package ministere.sante.senpna.stock.domain.port.in.lot;

import ministere.sante.senpna.stock.domain.command.LotCommands.BloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;

public interface BloquerLotUseCase {
    LotDetail bloquer(BloquerLotCommand command);
}
