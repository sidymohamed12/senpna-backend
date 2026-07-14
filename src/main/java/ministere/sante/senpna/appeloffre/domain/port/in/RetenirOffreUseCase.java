package ministere.sante.senpna.appeloffre.domain.port.in;

import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetenirOffreCommand;

public interface RetenirOffreUseCase {
    OffreDetail retenir(RetenirOffreCommand command);
}
