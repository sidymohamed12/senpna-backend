package ministere.sante.senpna.actualite.domain.port.in;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualitePage;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ListActualitesQuery;

public interface ListActualitesUseCase {
    ActualitePage lister(ListActualitesQuery query);
}
