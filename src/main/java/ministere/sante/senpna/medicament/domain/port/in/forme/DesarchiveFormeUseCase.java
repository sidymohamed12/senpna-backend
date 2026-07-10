package ministere.sante.senpna.medicament.domain.port.in.forme;

import ministere.sante.senpna.medicament.domain.command.FormeCommands.DesarchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;

public interface DesarchiveFormeUseCase {
    FormeDetail desarchiver(DesarchiveFormeCommand command);
}
