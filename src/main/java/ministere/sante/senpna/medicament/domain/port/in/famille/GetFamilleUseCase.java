package ministere.sante.senpna.medicament.domain.port.in.famille;

import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.GetFamilleQuery;

public interface GetFamilleUseCase {
    FamilleDetail obtenir(GetFamilleQuery query);
}
