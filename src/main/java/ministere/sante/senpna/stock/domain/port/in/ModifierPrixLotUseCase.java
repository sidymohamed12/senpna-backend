package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.LotCommands.ModifierPrixLotCommand;

public interface ModifierPrixLotUseCase {
    LotDetail modifierPrix(ModifierPrixLotCommand command);
}
