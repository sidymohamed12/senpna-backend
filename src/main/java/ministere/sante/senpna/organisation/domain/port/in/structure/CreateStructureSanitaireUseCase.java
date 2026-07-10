package ministere.sante.senpna.organisation.domain.port.in.structure;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;

public interface CreateStructureSanitaireUseCase {
    StructureSanitaireDetail creer(CreateStructureSanitaireCommand command);
}
