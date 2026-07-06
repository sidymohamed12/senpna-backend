package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.RemettreEnBrouillonProjetCommand;

public interface RemettreEnBrouillonProjetUseCase {
    ProjetDetail remettreEnBrouillon(RemettreEnBrouillonProjetCommand command);
}
