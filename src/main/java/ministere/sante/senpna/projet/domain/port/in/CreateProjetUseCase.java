package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.CreateProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;

public interface CreateProjetUseCase {
    ProjetDetail creer(CreateProjetCommand command);
}
