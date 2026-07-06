package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.UpdateProjetCommand;

public interface UpdateProjetUseCase {
    ProjetDetail modifier(UpdateProjetCommand command);
}
