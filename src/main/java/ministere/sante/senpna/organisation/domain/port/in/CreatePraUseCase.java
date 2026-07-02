package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreatePraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;

public interface CreatePraUseCase {
    EntrepotDetail creer(CreatePraCommand command);
}
