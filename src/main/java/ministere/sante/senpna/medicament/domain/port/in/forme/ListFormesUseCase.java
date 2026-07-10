package ministere.sante.senpna.medicament.domain.port.in.forme;

import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormePage;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.ListFormesQuery;

public interface ListFormesUseCase {
    FormePage lister(ListFormesQuery query);
}
