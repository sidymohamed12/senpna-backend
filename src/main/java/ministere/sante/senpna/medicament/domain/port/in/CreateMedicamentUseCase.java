package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;

public interface CreateMedicamentUseCase {
    MedicamentDetail creer(CreateMedicamentCommand command);
}
