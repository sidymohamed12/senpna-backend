package ministere.sante.senpna.actualite.domain.port.in;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.GetActualiteQuery;

public interface GetActualitePubliqueUseCase {
    ActualiteDetail obtenirPublique(GetActualiteQuery query);
}
