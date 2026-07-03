package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;

public interface DesarchiveFamilleUseCase {
    FamilleDetail desarchiver(DesarchiveFamilleCommand command);
}
