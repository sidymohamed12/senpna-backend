package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToStructureCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;

public interface AssignUserToStructureUseCase {
    UserAffectationDetail affecter(AssignUserToStructureCommand command);
}
