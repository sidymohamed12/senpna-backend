package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UpdateStructureSanitaireCommand;

public interface UpdateStructureSanitaireUseCase {
    StructureSanitaireDetail modifier(UpdateStructureSanitaireCommand command);
}
