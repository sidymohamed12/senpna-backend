package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetStructureSanitaireQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;

public interface GetStructureSanitaireUseCase {
    StructureSanitaireDetail obtenir(GetStructureSanitaireQuery query);
}
