package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;

public interface CreateFamilleUseCase {
    FamilleDetail creer(CreateFamilleCommand command);
}
