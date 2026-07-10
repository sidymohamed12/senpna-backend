package ministere.sante.senpna.organisation.domain.port.in.entrepot;

import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.GetEntrepotQuery;

public interface GetEntrepotUseCase {
    EntrepotDetail obtenir(GetEntrepotQuery query);
}
