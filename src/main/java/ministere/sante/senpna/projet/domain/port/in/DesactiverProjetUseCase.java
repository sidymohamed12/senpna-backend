package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.DesactiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;

public interface DesactiverProjetUseCase {
    ProjetDetail desactiver(DesactiverProjetCommand command);
}
