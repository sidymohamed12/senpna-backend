package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;

public interface DesarchiveFormeUseCase {
    FormeDetail desarchiver(DesarchiveFormeCommand command);
}
