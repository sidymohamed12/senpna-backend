package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ArchiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;

public interface ArchiverProjetUseCase {
    ProjetDetail archiver(ArchiverProjetCommand command);
}
