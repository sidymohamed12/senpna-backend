package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveConditionnementCommand;

public interface DesarchiveConditionnementUseCase {
    ConditionnementDetail desarchiver(DesarchiveConditionnementCommand command);
}
