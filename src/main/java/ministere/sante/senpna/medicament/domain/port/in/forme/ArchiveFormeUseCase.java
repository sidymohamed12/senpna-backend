package ministere.sante.senpna.medicament.domain.port.in.forme;

import ministere.sante.senpna.medicament.domain.command.FormeCommands.ArchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;

public interface ArchiveFormeUseCase {
    FormeDetail archiver(ArchiveFormeCommand command);
}
