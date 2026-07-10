package ministere.sante.senpna.medicament.domain.port.in.famille;

import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ListFamillesQuery;

public interface ListFamillesUseCase {
    FamillePage lister(ListFamillesQuery query);
}
