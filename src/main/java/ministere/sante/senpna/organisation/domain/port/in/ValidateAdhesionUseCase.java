package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ValidateAdhesionCommand;

public interface ValidateAdhesionUseCase {
    StructureSanitaireDetail valider(ValidateAdhesionCommand command);
}
