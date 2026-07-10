package ministere.sante.senpna.organisation.domain.port.in.entrepot;

import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ListEntrepotsQuery;

public interface ListEntrepotsUseCase {
    EntrepotPage lister(ListEntrepotsQuery query);
}
