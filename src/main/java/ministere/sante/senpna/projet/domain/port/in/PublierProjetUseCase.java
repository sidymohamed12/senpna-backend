package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.PublierProjetCommand;

public interface PublierProjetUseCase {
    ProjetDetail publier(PublierProjetCommand command);
}
