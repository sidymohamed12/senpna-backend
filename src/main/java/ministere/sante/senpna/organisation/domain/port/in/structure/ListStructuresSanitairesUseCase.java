package ministere.sante.senpna.organisation.domain.port.in.structure;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListStructuresSanitairesQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitairePage;

public interface ListStructuresSanitairesUseCase {
    StructureSanitairePage lister(ListStructuresSanitairesQuery query);
}
