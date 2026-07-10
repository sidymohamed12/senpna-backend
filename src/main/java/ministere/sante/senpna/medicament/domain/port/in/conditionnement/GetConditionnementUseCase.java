package ministere.sante.senpna.medicament.domain.port.in.conditionnement;

import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.GetConditionnementQuery;

public interface GetConditionnementUseCase {
    ConditionnementDetail obtenir(GetConditionnementQuery query);
}
