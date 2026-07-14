package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.GetOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;

public interface GetOffreUseCase {
    OffreDetail obtenir(GetOffreQuery query);
}
