package ministere.sante.senpna.actualite.domain.port.in;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.CreateActualiteCommand;

public interface CreateActualiteUseCase {
    ActualiteDetail creer(CreateActualiteCommand command);
}
