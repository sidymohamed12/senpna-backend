package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToEntrepotCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;

public interface AssignUserToEntrepotUseCase {
    UserAffectationDetail affecter(AssignUserToEntrepotCommand command);
}
