package ministere.sante.senpna.medicament.domain.port.in.forme;

import ministere.sante.senpna.medicament.domain.command.FormeCommands.CreateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;

public interface CreateFormeUseCase {
    FormeDetail creer(CreateFormeCommand command);
}
