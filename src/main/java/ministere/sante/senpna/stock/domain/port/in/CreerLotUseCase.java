package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.LotCommands.CreerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;

public interface CreerLotUseCase {
    LotDetail creer(CreerLotCommand command);
}
