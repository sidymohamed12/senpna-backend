package ministere.sante.senpna.organisation.domain.port.in;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListEntrepotsQuery;

public interface ListEntrepotsUseCase {
    EntrepotPage lister(ListEntrepotsQuery query);
}
