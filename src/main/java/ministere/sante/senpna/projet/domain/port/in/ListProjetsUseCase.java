package ministere.sante.senpna.projet.domain.port.in;

import ministere.sante.senpna.projet.domain.command.ProjetCommands.ListProjetsQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetPage;

public interface ListProjetsUseCase {
    ProjetPage lister(ListProjetsQuery query);
}
