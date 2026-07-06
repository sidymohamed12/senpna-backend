package ministere.sante.senpna.actualite.domain.port.in;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.DesactiverActualiteCommand;

public interface DesactiverActualiteUseCase {
    ActualiteDetail desactiver(DesactiverActualiteCommand command);
}
