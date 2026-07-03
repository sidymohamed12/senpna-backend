package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;

public interface DesarchiveMedicamentUseCase {
    MedicamentDetail desarchiver(DesarchiveMedicamentCommand command);
}
