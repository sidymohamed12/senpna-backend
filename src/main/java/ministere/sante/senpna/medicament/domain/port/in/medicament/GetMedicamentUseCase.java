package ministere.sante.senpna.medicament.domain.port.in.medicament;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;

public interface GetMedicamentUseCase {
    MedicamentDetail obtenir(GetMedicamentQuery query);
}
