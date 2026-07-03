package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;

public interface ArchiveFamilleUseCase {
    FamilleDetail archiver(ArchiveFamilleCommand command);
}
