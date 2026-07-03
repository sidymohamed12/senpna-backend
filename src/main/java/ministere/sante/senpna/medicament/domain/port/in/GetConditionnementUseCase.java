package ministere.sante.senpna.medicament.domain.port.in;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetConditionnementQuery;

public interface GetConditionnementUseCase {
    ConditionnementDetail obtenir(GetConditionnementQuery query);
}
