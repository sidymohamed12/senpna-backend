package ministere.sante.senpna.fournisseur.domain.port.in;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurPage;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ListFournisseursQuery;

public interface ListFournisseursUseCase {
    FournisseurPage lister(ListFournisseursQuery query);
}
