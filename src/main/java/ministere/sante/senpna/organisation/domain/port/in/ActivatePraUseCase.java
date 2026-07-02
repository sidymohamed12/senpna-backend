package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ActivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;

public interface ActivatePraUseCase {
    EntrepotDetail activer(ActivatePraCommand command);
}
