package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateMedicamentCommand;

public interface UpdateMedicamentUseCase {
    MedicamentDetail modifier(UpdateMedicamentCommand command);
}
