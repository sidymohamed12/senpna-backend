package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;

public interface ArchiveConditionnementUseCase {
    ConditionnementDetail archiver(ArchiveConditionnementCommand command);
}
