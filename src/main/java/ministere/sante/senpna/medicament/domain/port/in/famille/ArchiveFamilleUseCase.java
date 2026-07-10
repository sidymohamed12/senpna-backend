package ministere.sante.senpna.medicament.domain.port.in.famille;

import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;

public interface ArchiveFamilleUseCase {
    FamilleDetail archiver(ArchiveFamilleCommand command);
}
