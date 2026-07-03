package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;

public interface ListMedicamentsUseCase {
    MedicamentPage lister(ListMedicamentsQuery query);
}
