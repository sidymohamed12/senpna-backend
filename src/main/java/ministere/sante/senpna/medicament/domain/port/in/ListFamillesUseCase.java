package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFamillesQuery;

public interface ListFamillesUseCase {
    FamillePage lister(ListFamillesQuery query);
}
