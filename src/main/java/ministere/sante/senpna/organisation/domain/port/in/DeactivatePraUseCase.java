package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.DeactivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;

public interface DeactivatePraUseCase {
    EntrepotDetail desactiver(DeactivatePraCommand command);
}
