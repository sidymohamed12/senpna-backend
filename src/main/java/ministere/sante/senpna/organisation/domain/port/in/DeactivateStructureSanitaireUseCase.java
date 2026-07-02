package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.DeactivateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;

public interface DeactivateStructureSanitaireUseCase {
    StructureSanitaireDetail desactiver(DeactivateStructureSanitaireCommand command);
}
