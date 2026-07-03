package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFormesQuery;

public interface ListFormesUseCase {
    FormePage lister(ListFormesQuery query);
}
