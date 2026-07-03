package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;

public interface CreateFormeUseCase {
    FormeDetail creer(CreateFormeCommand command);
}
