package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;

public interface ArchiveFormeUseCase {
    FormeDetail archiver(ArchiveFormeCommand command);
}
