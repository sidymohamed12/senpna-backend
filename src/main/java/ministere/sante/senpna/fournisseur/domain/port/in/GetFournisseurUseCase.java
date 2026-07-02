package ministere.sante.senpna.fournisseur.domain.port.in;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;

public interface GetFournisseurUseCase {
    FournisseurDetail obtenir(GetFournisseurQuery query);
}
