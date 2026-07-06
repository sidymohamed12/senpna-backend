package ministere.sante.senpna.actualite.domain.port.in;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.PublierActualiteCommand;

public interface PublierActualiteUseCase {
    ActualiteDetail publier(PublierActualiteCommand command);
}
