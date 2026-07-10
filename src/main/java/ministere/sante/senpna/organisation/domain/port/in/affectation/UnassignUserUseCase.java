package ministere.sante.senpna.organisation.domain.port.in.affectation;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UnassignUserCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;

public interface UnassignUserUseCase {
    UserAffectationDetail retirer(UnassignUserCommand command);
}
