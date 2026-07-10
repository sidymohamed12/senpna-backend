package ministere.sante.senpna.medicament.domain.port.in.medicament;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;

public interface ArchiveMedicamentUseCase {
    MedicamentDetail archiver(ArchiveMedicamentCommand command);
}
