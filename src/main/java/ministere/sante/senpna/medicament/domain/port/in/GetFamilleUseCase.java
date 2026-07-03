package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetFamilleQuery;

public interface GetFamilleUseCase {
    FamilleDetail obtenir(GetFamilleQuery query);
}
