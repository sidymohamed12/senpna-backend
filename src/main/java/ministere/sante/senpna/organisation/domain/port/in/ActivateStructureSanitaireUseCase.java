package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ActivateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;

public interface ActivateStructureSanitaireUseCase {
    StructureSanitaireDetail activer(ActivateStructureSanitaireCommand command);
}
