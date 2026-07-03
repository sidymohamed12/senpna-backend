package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetFormeQuery;

public interface GetFormeUseCase {
    FormeDetail obtenir(GetFormeQuery query);
}
