package ministere.sante.senpna.actualite.domain.port.in;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.UpdateActualiteCommand;

public interface UpdateActualiteUseCase {
    ActualiteDetail modifier(UpdateActualiteCommand command);
}
