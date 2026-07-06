package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.GetProjetQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;

public interface GetProjetUseCase {
    ProjetDetail obtenir(GetProjetQuery query);
}
