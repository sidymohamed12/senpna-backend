package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateFamilleCommand;

public interface UpdateFamilleUseCase {
    FamilleDetail modifier(UpdateFamilleCommand command);
}
