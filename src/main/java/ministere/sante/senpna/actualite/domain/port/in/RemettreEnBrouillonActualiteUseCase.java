package ministere.sante.senpna.actualite.domain.port.in;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.RemettreEnBrouillonActualiteCommand;

public interface RemettreEnBrouillonActualiteUseCase {
    ActualiteDetail remettreEnBrouillon(RemettreEnBrouillonActualiteCommand command);
}
