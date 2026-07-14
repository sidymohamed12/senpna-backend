package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListMesOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;

public interface ListMesOffresUseCase {
    OffrePage lister(ListMesOffresQuery query);
}
