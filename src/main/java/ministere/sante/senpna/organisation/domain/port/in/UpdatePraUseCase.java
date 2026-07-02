package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UpdatePraCommand;

public interface UpdatePraUseCase {
    EntrepotDetail modifier(UpdatePraCommand command);
}
