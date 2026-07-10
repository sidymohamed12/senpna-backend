package ministere.sante.senpna.medicament.domain.port.in.forme;

import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.UpdateFormeCommand;

public interface UpdateFormeUseCase {
    FormeDetail modifier(UpdateFormeCommand command);
}
