package ministere.sante.senpna.medicament.domain.port.in.famille;

import ministere.sante.senpna.medicament.domain.command.FamilleCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;

public interface CreateFamilleUseCase {
    FamilleDetail creer(CreateFamilleCommand command);
}
