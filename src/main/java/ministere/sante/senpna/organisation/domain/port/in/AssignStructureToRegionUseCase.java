package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignStructureToRegionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;

public interface AssignStructureToRegionUseCase {
    StructureSanitaireDetail affecter(AssignStructureToRegionCommand command);
}
