package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateFormeCommand;

public interface UpdateFormeUseCase {
    FormeDetail modifier(UpdateFormeCommand command);
}
