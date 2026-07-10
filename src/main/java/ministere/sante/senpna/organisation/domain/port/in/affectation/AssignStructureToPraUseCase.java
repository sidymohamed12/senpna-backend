package ministere.sante.senpna.organisation.domain.port.in.affectation;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignStructureToPraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;

public interface AssignStructureToPraUseCase {
    StructureSanitaireDetail affecter(AssignStructureToPraCommand command);
}
